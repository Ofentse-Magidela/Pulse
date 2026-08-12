package com.ofentse.pulse.auth;

import com.ofentse.pulse.emailverification.EmailVerification;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private boolean emailVerified;

    @OneToMany(mappedBy = "user")
    private List<EmailVerification> emailVerifications = new ArrayList<>();

    public void addEmailVerification(EmailVerification email) {
        if (email != null) {
            this.emailVerifications.add(email);
            email.setUser(this);
        }
    }
}
