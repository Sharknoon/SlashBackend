let conn;

let slashHost = window.location.host;
let slashPath = window.location.pathname;

function connect() {
    const endpoints = document.forms[0];
    let link = "";
    let i;
    for (i = 0; i < endpoints.length; i++) {
        if (endpoints[i].checked) {
            link = endpoints[i].labels[0].innerText;
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
    for (let i = 0; i < endpoints.length; i++) {
        endpoints[i].readOnly = false;
    }
}

function disconnect() {
    conn.close();
    clearLog();
    document.getElementById("disconnect").setAttribute("disabled", "disabled");
    document.getElementById("connect").removeAttribute("disabled");
    const endpoints = document.forms;
    for (let i = 0; i < endpoints.length; i++) {
        endpoints[i].readOnly = true;
    }
}

function sendMessage() {
    conn.send(document.getElementById("msg").value);
}

function clearLog() {
    document.getElementById("out").value = "";
}