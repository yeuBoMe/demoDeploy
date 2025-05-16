package com.computer.demoComputer.domain;

import java.io.Serializable;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "products")
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @Size(min = 5, message = "Tên sản phẩm phải tối thiểu 5 kí tự!")
    private String name;

    @NotNull
    @DecimalMin(value = "0", inclusive = false, message = "Giá phải lớn hơn 0!")
    private double price;

    private String image;

    @NotNull
    @NotEmpty(message = "Miêu tả chi tiết không được để trống!")
    @Column(columnDefinition = "TEXT")
    private String detailDesc;

    @NotNull
    @NotEmpty(message = "Miêu tả ngắn gọn không được để trống!")
    private String shortDesc;

    @NotNull
    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1!")
    private long quantity;

    private long sold;
    private String factory;
    private String target;

    /*Không cần vì không cần biết có sản phẩm có trong bao nhiêu chi tiết đơn hàng
    @OneToMany(mappedBy = "product")
    private List<OrderDetail> orderDetails;
    */

    public Product() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDetailDesc() {
        return detailDesc;
    }

    public void setDetailDesc(String detailDesc) {
        this.detailDesc = detailDesc;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public long getSold() {
        return sold;
    }

    public void setSold(long sold) {
        this.sold = sold;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", image='" + image + '\'' +
                ", detailDesc='" + detailDesc + '\'' +
                ", shortDesc='" + shortDesc + '\'' +
                ", quantity=" + quantity +
                ", sold=" + sold +
                ", factory='" + factory + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}

