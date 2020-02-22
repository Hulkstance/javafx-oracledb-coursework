package helpers;

/**
 * Represents the projects.
 */
public class Project {

    private int id;
    private String name;
    private String description;
    private String customer;
    private String service;

    public Project(int id, String name, String description, String customer, String service) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.customer = customer;
        this.service = service;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCustomer() {
        return customer;
    }

    public String getService() {
        return service;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public void setService(String service) {
        this.service = service;
    }
}
