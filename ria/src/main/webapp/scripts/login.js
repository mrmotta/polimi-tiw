window.addEventListener('load', function () {
    let myButton = document.getElementById("submit");
    document.body.addEventListener("keydown", function (event) {
        if (event.key == 'Enter') {
            myButton.click();
        }
    });
}, false);

function checkLogin() {
    let errorMessage = document.getElementById("errorMessage");
    let username = document.getElementById("username").value;
    let password = document.getElementById("password").value;

    if (username.length == 0 || password.length == 0) {
        errorMessage.innerHTML = "Cannot leave fields empty.";
        return;
    }
    let xhttp = new XMLHttpRequest();

    let postObj = {
        username: username,
        password: password,
    }

    xhttp.onload = function () {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if (xhttp.status == 200) {
                let user = xhttp.responseText;
                localStorage.setItem('username', user.substring(0, user.length - 1));
                window.location.href = "/ria/home/";
            } else {
                errorMessage.innerHTML = xhttp.responseText;
            }
        }
    }

    xhttp.open("POST", "/ria/login/");
    xhttp.setRequestHeader('Content-type', 'application/json; charset=UTF-8');
    xhttp.send(JSON.stringify(postObj));

    return;
}
