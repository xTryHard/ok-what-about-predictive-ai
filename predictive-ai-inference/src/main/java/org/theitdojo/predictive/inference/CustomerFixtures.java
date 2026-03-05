package org.theitdojo.predictive.inference;

import org.theitdojo.predictive.core.Customer;

import java.util.List;

public final class CustomerFixtures {
    private CustomerFixtures() {}

    public static List<Customer> smokeTestCustomers() {
        return List.of(
                new Customer("7590-VHVEG","Female",0,"Yes","No",1,"No","No phone service","DSL","No","Yes","No","No","No","No","Month-to-month","Yes","Electronic check",29.85f,29.85f,"No"),
                new Customer("5575-GNVDE","Male",0,"No","No",34,"Yes","No","DSL","Yes","No","Yes","No","No","No","One year","No","Mailed check",56.95f,1889.5f,"No"),
                new Customer("3668-QPYBK","Male",0,"No","No",2,"Yes","No","DSL","Yes","Yes","No","No","No","No","Month-to-month","Yes","Mailed check",53.85f,108.15f,"Yes"),
                new Customer("7795-CFOCW","Male",0,"No","No",45,"No","No phone service","DSL","Yes","No","Yes","Yes","No","No","One year","No","Bank transfer (automatic)",42.3f,1840.75f,"No"),
                new Customer("9237-HQITU","Female",0,"No","No",2,"Yes","No","Fiber optic","No","No","No","No","No","No","Month-to-month","Yes","Electronic check",70.7f,151.65f,"Yes"),
                new Customer("9305-CDSKC","Female",0,"No","No",8,"Yes","Yes","Fiber optic","No","No","Yes","No","Yes","Yes","Month-to-month","Yes","Electronic check",99.65f,820.5f,"Yes"),
                new Customer("1452-KIOVK","Male",0,"No","Yes",22,"Yes","Yes","Fiber optic","No","Yes","No","No","Yes","No","Month-to-month","Yes","Credit card (automatic)",89.1f,1949.4f,"No"),
                new Customer("6713-OKOMC","Female",0,"No","No",10,"No","No phone service","DSL","Yes","No","No","No","No","No","Month-to-month","No","Mailed check",29.75f,301.9f,"No"),
                new Customer("7892-POOKP","Female",0,"Yes","No",28,"Yes","Yes","Fiber optic","No","No","Yes","Yes","Yes","Yes","Month-to-month","Yes","Electronic check",104.8f,3046.05f,"Yes"),
                new Customer("6388-TABGU","Male",0,"No","Yes",62,"Yes","No","DSL","Yes","Yes","No","No","No","No","One year","No","Bank transfer (automatic)",56.15f,3487.95f,"No")
        );
    }
}
