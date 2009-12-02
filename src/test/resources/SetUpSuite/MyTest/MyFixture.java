public class MyFixture {
  private String firstName;
  private String lastName;
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }
  
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
  
  public boolean valid() {
    return false;
  }
}