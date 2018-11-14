let conn;

let slashHost = window.location.host;
let slashPath = window.location.pathname;

function connect() {
    const coffee = document.forms[0];
    let link = "";
    let i;
    for (i = 0; i < coffee.length; i++) {
        if (coffee[i].checked) {
            link = coffee[i].labels[0].innerText;
            link = "wss://" + slashHost + slashPath + link.substr(1);
            break;
        }
    }
    conn = new WebSocket(link);
    conn.onmessage = (msg) => {
        let cValue = document.getElementById("out").value;
        document.getElementById("out").value = cValue + msg.data + "\n";
    };
    document.getElementById("connect").setAttribute("disabled", "disabled");
    document.getElementById("disconnect").removeAttribute("disabled");
}

function disconnect() {
    conn.close();
    clearLog();
    document.getElementById("disconnect").setAttribute("disabled", "disabled");
    document.getElementById("connect").removeAttribute("disabled");
}

function sendMessage() {
    conn.send(document.getElementById("msg").value);
}

function clearLog() {
    document.getElementById("out").value = "";
}