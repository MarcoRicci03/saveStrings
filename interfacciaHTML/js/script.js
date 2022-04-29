function loadDoc() {
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            document.getElementById("demo").innerHTML = this.responseText;
        }
    };
    xhttp.open("GET", "http://localhost:8080/SaveStrings/getToken?username=marco&password=ricci", true);
    xhttp.send();
    xhttp.getJSON('my/service', function (data) {
        $('#myimage').attr('src', data.url);
    });
}
