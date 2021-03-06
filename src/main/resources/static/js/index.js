const serverRunPath = "http://localhost:8080/run";

const outPut = document.getElementById("output");
const runBtn = document.getElementById("run-btn");

// Get editor and configure it
const editor = CodeMirror(document.querySelector('#editor'), {
    tabSize: 2,
    lineNumbers: true,
    mode: 'text/x-csrc',
    theme: 'monokai'
});

runBtn.onclick = function() {
    let data = editor.getValue();
    let xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        output.innerHTML = xhr.response;
    }
    xhr.open('POST', serverRunPath);
    xhr.setRequestHeader('Content-Type', 'text/plain');
    xhr.send(data);
}
