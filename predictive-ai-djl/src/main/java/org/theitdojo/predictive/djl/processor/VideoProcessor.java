package org.theitdojo.predictive.djl.processor;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import org.theitdojo.predictive.djl.detector.ObjectDetector;
import org.theitdojo.predictive.djl.renderer.DetectionRenderer;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoProcessor implements MediaProcessor {

    private static final Logger logger = LoggerFactory.getLogger(VideoProcessor.class);
    private static final int FRAME_SKIP = 2;

    private final ObjectDetector detector;

    public VideoProcessor(ObjectDetector detector) {
        this.detector = detector;
    }

    @Override
    public void process(String input, Path output) throws Exception {
        try (Java2DFrameConverter converter = new Java2DFrameConverter();
             FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input)) {

            grabber.start();
            int width = grabber.getImageWidth();
            int height = grabber.getImageHeight();
            double frameRate = grabber.getVideoFrameRate();
            int totalFrames = grabber.getLengthInFrames();

            System.out.printf("Video: %dx%d, %.2f fps, ~%d frames%n", width, height, frameRate, totalFrames);
            System.out.println("Inference runs every " + FRAME_SKIP + " frames (others reuse last result).");

            logger.info("Video: {}x{}, {} fps, ~{} frames", width, height,
                    String.format("%.2f", frameRate), totalFrames);
            logger.info("Frame skip: every {}nd frame is inferred, rest reuse last result", FRAME_SKIP);

            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(output.toString(), width, height)) {
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setFrameRate(frameRate);
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                recorder.setVideoBitrate(grabber.getVideoBitrate());
                recorder.start();

                Frame frame = null;
                int frameCount = 0;
                DetectedObjects lastDetection = null;

                while ((frame = grabber.grabImage()) != null) {
                    frameCount++;

                    // FFmpeg may deliver BGR; normalize to TYPE_INT_RGB for Java2D
                    BufferedImage raw = converter.convert(frame);
                    if (raw == null) continue;
                    BufferedImage buffered = new BufferedImage(raw.getWidth(), raw.getHeight(), BufferedImage.TYPE_INT_RGB);
                    buffered.getGraphics().drawImage(raw, 0, 0, null);

                    // Run inference only every Nth frame; reuse last result otherwise
                    if (frameCount % FRAME_SKIP == 0 || lastDetection == null) {
                        Image djlImage = ImageFactory.getInstance().fromImage(buffered);
                        lastDetection = detector.detect(djlImage);
                    }

                    // Annotate and record
                    Image djlImage = ImageFactory.getInstance().fromImage(buffered);
                    DetectionRenderer.draw(djlImage, lastDetection);

                    // Convert back to TYPE_3BYTE_BGR for FFmpeg (YUV420P output)
                    BufferedImage annotated = (BufferedImage) djlImage.getWrappedImage();
                    BufferedImage bgrOut = new BufferedImage(annotated.getWidth(), annotated.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                    bgrOut.getGraphics().drawImage(annotated, 0, 0, null);
                    recorder.record(converter.convert(bgrOut));

                    if (frameCount % 30 == 0 || frameCount == 1) {
                        System.out.printf("  Frame %d / %d (%.0f%%)%n",
                                frameCount, totalFrames, 100.0 * frameCount / Math.max(totalFrames, 1));
                        logger.info("Frame {} / {} ({}%)", frameCount, totalFrames,
                                String.format("%.0f", 100.0 * frameCount / Math.max(totalFrames, 1)));
                    }
                }
                System.out.println("Processed " + frameCount + " frames total.");
                logger.info("Processed {} frames total.", frameCount);
            }
        }
        logger.info("Annotated video saved to {}", output);
    }
}
