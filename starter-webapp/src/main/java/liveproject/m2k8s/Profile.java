package liveproject.m2k8s;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Email;

@Data
@Entity(name = "emp_profile")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Profile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @NotNull
  @Size(min=5, max=16, message="{username.size}")
  private String username;

  @NotNull
  @Size(min=5, max=25, message="{password.size}")
  private String password;
  
  @NotNull
  @Size(min=2, max=30, message="{firstName.size}")
  @Column(name = "first_name")
  private String firstName;

  @NotNull
  @Size(min=2, max=30, message="{lastName.size}")
  @Column(name = "last_name")
  private String lastName;
  
  @NotNull
  @Email
  private String email;

  private String imageFileName;
  private String imageFileContentType;

  public Profile(String username, String password, String firstName, String lastName, String email) {
    this(null, username, password, firstName, lastName, email, null, null);
  }

}
