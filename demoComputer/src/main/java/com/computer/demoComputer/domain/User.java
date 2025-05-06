package com.computer.demoComputer.domain;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @Email(message = "Email không hợp lệ", regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    private String email;

    @Size(min = 5, message = "Mật khẩu phải tối thiểu 5 kí tự!")
    private String password;

    @NotNull
    @Size(min = 5, message = "Họ tên phải tối thiểu 5 kí tự!")
    private String fullName;

    @Size(min = 5, message = "Địa chỉ phải tối thiểu 5 kí tự!")
    private String address;

    @Min(value = 10, message = "Số điện thoại phải tối thiểu 10 kí tự!")
    private String phoneNumber;

    private String avatar;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Order> orders;

    @OneToOne(mappedBy = "user")
    private Cart cart;

    public User() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", email=" + email + ", password=" + password + ", fullName=" + fullName
                + ", address=" + address + ", phoneNumber=" + phoneNumber + ", avatar=" + avatar + ", role=" + role
                + ", orders=" + orders + "]";
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

}

// package com.computer.demoComputer.domain;

// import java.io.Serializable;
// import java.util.List;

// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;
// import jakarta.persistence.OneToMany;
// import jakarta.persistence.OneToOne;
// import jakarta.persistence.Table;
// import jakarta.validation.constraints.Email;
// import jakarta.validation.constraints.NotNull;
// import jakarta.validation.constraints.Pattern;
// import jakarta.validation.constraints.Size;

// @Entity
// @Table(name = "users")
// public class User implements Serializable {
//     private static final long serialVersionUID = 1L;

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private long id;

//     @NotNull
//     @Email(message = "Email không hợp lệ", regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
//     private String email;

//     @NotNull // Thêm @NotNull
//     @Size(min = 5, message = "Mật khẩu phải tối thiểu 5 ký tự!")
//     private String password;

//     @NotNull
//     @Size(min = 5, message = "Họ tên phải tối thiểu 5 ký tự!")
//     private String fullName;

//     @NotNull // Thêm @NotNull
//     @Size(min = 5, message = "Địa chỉ phải tối thiểu 5 ký tự!")
//     private String address;

//     @Size(min = 10, message = "Số điện thoại phải tối thiểu 10 ký tự!") // Sửa @Min thành @Size
//     @Pattern(regexp = "\\d{10,}", message = "Số điện thoại chỉ được chứa chữ số và tối thiểu 10 ký tự!") // Thêm ràng
//                                                                                                          // buộc regex
//     private String phoneNumber;

//     private String avatar;

//     @ManyToOne
//     @JoinColumn(name = "role_id")
//     private Role role;

//     @OneToMany(mappedBy = "user")
//     private List<Order> orders;

//     @OneToOne(mappedBy = "user")
//     private Cart cart;

//     public User() {
//     }

//     public long getId() {
//         return id;
//     }

//     public void setId(long id) {
//         this.id = id;
//     }

//     public String getEmail() {
//         return email;
//     }

//     public void setEmail(String email) {
//         this.email = email;
//     }

//     public String getPassword() {
//         return password;
//     }

//     public void setPassword(String password) {
//         this.password = password;
//     }

//     public String getFullName() {
//         return fullName;
//     }

//     public void setFullName(String fullName) {
//         this.fullName = fullName;
//     }

//     public String getAddress() {
//         return address;
//     }

//     public void setAddress(String address) {
//         this.address = address;
//     }

//     public String getPhoneNumber() {
//         return phoneNumber;
//     }

//     public void setPhoneNumber(String phoneNumber) {
//         this.phoneNumber = phoneNumber;
//     }

//     public String getAvatar() {
//         return avatar;
//     }

//     public void setAvatar(String avatar) {
//         this.avatar = avatar;
//     }

//     public Role getRole() {
//         return role;
//     }

//     public void setRole(Role role) {
//         this.role = role;
//     }

//     public List<Order> getOrders() {
//         return orders;
//     }

//     public void setOrders(List<Order> orders) {
//         this.orders = orders;
//     }

//     public Cart getCart() {
//         return cart;
//     }

//     public void setCart(Cart cart) {
//         this.cart = cart;
//     }

//     @Override
//     public String toString() {
//         return "User [id=" + id + ", email=" + email + ", password=" + password + ", fullName=" + fullName
//                 + ", address=" + address + ", phoneNumber=" + phoneNumber + ", avatar=" + avatar + ", role=" + role
//                 + "]";
//     }
// }