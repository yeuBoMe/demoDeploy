/*
    $(): cú pháp jQuery để chọn phần tử html
    $(document).ready(...) đoạn mã bên trong chỉ chạy khi trang web đã tải xong
    $("#avatarFile"): chọn phần tử html có id="avatarFile"
    e.target.files[0]: Lấy file đầu tiên mà người dùng chọn
    URL.createObjectURL(...): Tạo một URL tạm thời để trình duyệt show ảnh đó
    $("#avatarPreview"): Chọn phần tử <img> có id="avatarPreview"
    .attr("src", imgURL"): Đặt thuộc tính src của <img> thành đường dẫn ảnh imgURL, giúp show ảnh.
    .css("display", "block"): Đảm bảo ảnh được hiển thị bằng cách đặt display: block.
    display: block: chiếm toàn bộ chiều rộng của dòng chứa nó.
*/

$(document).ready(() => {
    const avatarFile = $("#avatarFile");
    avatarFile.change(function(e) {
        if(e.target.files.length > 0) {
            const imgURL = URL.createObjectURL(e.target.files[0]);
            $("#avatarPreview").attr("src", imgURL).css({"display": "block"});
        }
    });
});