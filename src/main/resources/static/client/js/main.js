(function ($) {
  "use strict";

  // Spinner
  var spinner = function () {
    setTimeout(function () {
      if ($("#spinner").length > 0) {
        $("#spinner").removeClass("show");
      }
    }, 1);
  };
  spinner(0);

  // Fixed Navbar
  $(window).scroll(function () {
    if ($(window).width() < 992) {
      if ($(this).scrollTop() > 55) {
        $(".fixed-top").addClass("shadow");
      } else {
        $(".fixed-top").removeClass("shadow");
      }
    } else {
      if ($(this).scrollTop() > 55) {
        $(".fixed-top").addClass("shadow").css("top", 0);
      } else {
        $(".fixed-top").removeClass("shadow").css("top", 0);
      }
    }
  });

  // Back to top button
  $(window).scroll(function () {
    if ($(this).scrollTop() > 300) {
      $(".back-to-top").fadeIn("slow");
    } else {
      $(".back-to-top").fadeOut("slow");
    }
  });
  $(".back-to-top").click(function () {
    $("html, body").animate({ scrollTop: 0 }, 1500, "easeInOutExpo");
    return false;
  });

  // Testimonial carousel
  $(".testimonial-carousel").owlCarousel({
    autoplay: true,
    smartSpeed: 2000,
    center: false,
    dots: true,
    loop: true,
    margin: 25,
    nav: true,
    navText: [
      '<i class="bi bi-arrow-left"></i>',
      '<i class="bi bi-arrow-right"></i>',
    ],
    responsiveClass: true,
    responsive: {
      0: {
        items: 1,
      },
      576: {
        items: 1,
      },
      768: {
        items: 1,
      },
      992: {
        items: 2,
      },
      1200: {
        items: 2,
      },
    },
  });

  // vegetable carousel
  $(".vegetable-carousel").owlCarousel({
    autoplay: true,
    smartSpeed: 1500,
    center: false,
    dots: true,
    loop: true,
    margin: 25,
    nav: true,
    navText: [
      '<i class="bi bi-arrow-left"></i>',
      '<i class="bi bi-arrow-right"></i>',
    ],
    responsiveClass: true,
    responsive: {
      0: {
        items: 1,
      },
      576: {
        items: 1,
      },
      768: {
        items: 2,
      },
      992: {
        items: 3,
      },
      1200: {
        items: 4,
      },
    },
  });

  // Modal Video
  $(document).ready(function () {
    var $videoSrc;
    $(".btn-play").click(function () {
      $videoSrc = $(this).data("src");
    });
    console.log($videoSrc);

    $("#videoModal").on("shown.bs.modal", function (e) {
      $("#video").attr(
        "src",
        $videoSrc + "?autoplay=1&amp;modestbranding=1&amp;showinfo=0"
      );
    });

    $("#videoModal").on("hide.bs.modal", function (e) {
      $("#video").attr("src", $videoSrc);
    });

    // add active class to header
    const navElement = $("#navbarCollapse");
    const currentUrl = window.location.pathname;
    navElement.find("a.nav-link").each(function () {
      const link = $(this); // get the current link in the loop
      const href = link.attr("href"); // get the href attribute of the link

      if (href === currentUrl) {
        link.addClass("active"); // add 'active' class if the href matches the currentUrl
      } else {
        link.removeClass("active"); // remove 'active' class if the href does not match the currentUrl
      }
    });
  });

  // Product Quantity
  // $('.quantity button').on('click', function () {
  //     var button = $(this);
  //     var oldValue = button.parent().parent().find('input').val();
  //     if (button.hasClass('btn-plus')) {
  //         var newVal = parseFloat(oldValue) + 1;
  //     } else {
  //         if (oldValue > 0) {
  //             var newVal = parseFloat(oldValue) - 1;
  //         } else {
  //             newVal = 0;
  //         }
  //     }
  //     button.parent().parent().find('input').val(newVal);
  // });

  $(" .quantity button").on("click", function () {
    var button = $(this);
    var input = button.parent().parent().find("input");
    var oldValue = parseFloat(input.val());
    var price = parseFloat(input.attr("data-cart-detail-price"));
    var id = input.attr("data-cart-detail-id");

    var newVal = oldValue;
    if (button.hasClass("btn-plus")) {
      newVal = oldValue + 1;
    } else if (oldValue > 1) {
      newVal = oldValue - 1;
    }

    input.val(newVal);

    const index = input.attr("data-cart-detail-index");
    const el = document.getElementById(`cartDetails${index}.quantity`);
    $(el).val(newVal);

    var priceElement = $(`p[data-cart-detail-id='${id}']`);
    if (priceElement.length) {
      var newPrice = price * newVal;
      priceElement.text(formatCurrency(newPrice.toFixed(2)) + " đ");
    }

    // Cập nhật tổng tiền
    updateTotalPrice();
  });

  function updateTotalPrice() {
    var total = 0;

    // Lặp qua từng sản phẩm và tính tổng tiền
    $("input[data-cart-detail-price]").each(function () {
      var quantity = parseFloat($(this).val());
      var price = parseFloat($(this).attr("data-cart-detail-price"));
      total += quantity * price;
    });

    // Cập nhật tất cả các phần tử có chứa tổng tiền
    $("p[data-cart-total-price]").each(function () {
      $(this).text(formatCurrency(total.toFixed(2)) + " đ");
      $(this).attr("data-cart-total-price", total);
    });
  }

  function formatCurrency(value) {
    const formatter = new Intl.NumberFormat("vi-VN", {
      style: "decimal",
      minimumFractionDigits: 0,
    });

    let formatted = formatter.format(value);
    formatted = formatted.replace(/\./g, ",");
    return formatted;
  }

  // handle filter products
  $("#btn-filter").click(function (event) {
    event.preventDefault();

    let factoryArr = [];
    let targetArr = [];
    let priceArr = [];

    // Lấy giá trị từ các checkbox được chọn
    $("#factory-filter .form-check-input:checked").each(function () {
      factoryArr.push($(this).val());
    });

    $("#target-filter .form-check-input:checked").each(function () {
      targetArr.push($(this).val());
    });

    $("#price-filter .form-check-input:checked").each(function () {
      priceArr.push($(this).val());
    });

    // Lấy giá trị sắp xếp
    let sortValue = $('input[name="radio-sort"]:checked').val();

    const currentUrl = new URL(window.location.href);
    const searchParams = currentUrl.searchParams;

    // Đặt lại trang về 1 khi lọc
    searchParams.set("page", "1");

    // Cập nhật tham số sắp xếp
    searchParams.set("sort", sortValue);

    // Xử lý bộ lọc hãng sản xuất
    if (factoryArr.length > 0) {
      searchParams.set("factory", factoryArr.join(","));
    } else {
      searchParams.delete("factory"); // Xóa tham số nếu không có lựa chọn
    }

    // Xử lý bộ lọc mục đích sử dụng
    if (targetArr.length > 0) {
      searchParams.set("target", targetArr.join(","));
    } else {
      searchParams.delete("target");
    }

    // Xử lý bộ lọc mức giá
    if (priceArr.length > 0) {
      searchParams.set("price", priceArr.join(","));
    } else {
      searchParams.delete("price");
    }

    // Cập nhật URL và tải lại trang
    window.location.href = currentUrl.toString();
  });

  // Xử lý tự động tích các checkbox khi trang được tải
  $(document).ready(function () {
    const params = new URLSearchParams(window.location.search);

    // Tích các checkbox cho 'factory'
    if (params.has("factory")) {
      const factories = params.get("factory").split(",");
      factories.forEach((factory) => {
        $(`#factory-filter input[value="${factory}"]`).prop("checked", true);
      });
    }

    // Tích các checkbox cho 'target'
    if (params.has("target")) {
      const targets = params.get("target").split(",");
      targets.forEach((target) => {
        $(`#target-filter input[value="${target}"]`).prop("checked", true);
      });
    }

    // Tích các checkbox cho 'price'
    if (params.has("price")) {
      const prices = params.get("price").split(",");
      prices.forEach((price) => {
        $(`#price-filter input[value="${price}"]`).prop("checked", true);
      });
    }

    // Tích radio cho 'sort'
    if (params.has("sort")) {
      const sortValue = params.get("sort");
      $(`input[name="radio-sort"][value="${sortValue}"]`).prop("checked", true);
    }
  });
  
})(jQuery);
