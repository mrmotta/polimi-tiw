function checkSignup() {
    let errorMessage = document.getElementById("errorMessage");
    let email = document.getElementById("email").value;
    let username = document.getElementById("username").value;
    let password = document.getElementById("password").value;
    let repeat = document.getElementById("repeat_password").value;
    let xhttp = new XMLHttpRequest();

    if (email.length == 0 || username.length == 0 || password.length == 0 || repeat.length == 0) {
        errorMessage.innerHTML = "Can't leave fields empty.";
        return;
    }

    if (password != repeat) {
        errorMessage.innerHTML = "Password and repeat password must be equal.";
        return;
    }

    if (!validateEmail(email)) {
        errorMessage.innerHTML = "Invalid email format.";
        return;
    }

    let postObj = {
        username: username,
        email: email,
        password: password,
        repeat_password: repeat
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

    xhttp.open("POST", "/ria/signup/");
    xhttp.setRequestHeader('Content-type', 'application/json; charset=UTF-8');
    xhttp.send(JSON.stringify(postObj));

    return;
}

function validateEmail(email) {
    if (email.match(/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/)) {
        return true;
    } else {
        return false;
    }
}
