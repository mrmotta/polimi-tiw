let main_div
let title;
let callback_span;
let errorMessage;
let user;
let opened_modal = null;

window.addEventListener('load', function () {

    main_div = document.getElementById("main");
    title = document.getElementById("title");
    errorMessage = document.getElementById("errorMessage");
    user = this.localStorage.getItem("username");

    home();
}, false);

window.onclick = function (event) {
    if (event.target == opened_modal) {
        opened_modal.style.display = "none";
    }
}

function subfolder(id) {
    let xhttp = new XMLHttpRequest();
    errorMessage.innerHTML = "";
    xhttp.onload = function () {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if (xhttp.status == 200) {
                let ul = document.getElementById(`ul_subfolder_${id}`)
                json = JSON.parse(this.responseText);
                let array = json["items"];
                for (let i = 0; i < array.length; i++) {
                    ul.innerHTML += `<li draggable="true" id="file_${array[i]["id"]}" onclick="showFileDetailsModal(${array[i]["id"]})" ondragstart="drag(event)">${array[i]["name"]}.${array[i]["ext"]}</li>`;
                }
            } else {
                if (xhttp.responseText.length == 0) {
                    errorMessage.innerHTML = "An unexpected error occurred.";
                } else {
                    errorMessage.innerHTML = xhttp.responseText;
                }
            }
        }
    }
    xhttp.open("POST", `/ria/home/folder/${id}`);
    xhttp.send();
}

function home() {
    let xhttp = new XMLHttpRequest();

    errorMessage.innerHTML = "";
    xhttp.onload = function () {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if (xhttp.status == 200) {
                main_div.innerHTML = "";
                title.innerHTML = `Welcome, ${user}!`;
                json = JSON.parse(this.responseText);
                let table = document.createElement("table");
                table.classList.add("center");
                table.classList.add("decorate");

                main_div.appendChild(table);

                let array = json["items"];

                for (let i = 0; i < array.length; i++) {
                    let tr = document.createElement("tr");
                    let td = document.createElement("td");
                    tr.appendChild(td);
                    table.appendChild(tr);
                    td.id = `folder_${array[i]["id"]}`;
                    td.draggable = true;
                    td.ondragstart = drag;
                    td.innerHTML = `${array[i]["name"]} <button type="button" onclick="showSubfolderModal(${array[i]["id"]}, '${array[i]["name"]}')">+ <i class="fa fa-folder"></i></button>`;

                    td = document.createElement("td");
                    tr.appendChild(td);
                    let ul = document.createElement("ul");
                    td.appendChild(ul);
                    for (let j = 0; j < array[i]["subfolders"].length; j++) {
                        let li = document.createElement("li");
                        li.id = `folder_${array[i]["subfolders"][j]["id"]}`;
                        li.ondrop = dropFolder;
                        li.ondragover = allowDrop;
                        li.ondragstart = drag;
                        li.draggable = true;
                        li.innerHTML = `${array[i]["subfolders"][j]["name"]} <button ondragover="return false;" type="button" onclick="showFileModal(${array[i]["subfolders"][j]["id"]}, '${array[i]["subfolders"][j]["name"]}')">+ <i class="fa fa-file"></i></button>`;

                        let ul2 = document.createElement("ul");
                        ul2.id = `ul_subfolder_${array[i]["subfolders"][j]["id"]}`;
                        li.appendChild(ul2);
                        ul.appendChild(li);

                        subfolder(array[i]["subfolders"][j]["id"]);
                    }
                }

            } else {
                if (xhttp.responseText.length == 0) {
                    errorMessage.innerHTML = "An unexpected error occurred.";
                } else {
                    errorMessage.innerHTML = xhttp.responseText;
                }
            }

        }
    }
    xhttp.open("POST", "/ria/home/");
    xhttp.send();
}

function move(folder_id, file_id, ev, data) {
    let xhttp = new XMLHttpRequest();

    errorMessage.innerHTML = "";
    xhttp.onload = function () {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if (xhttp.status == 200) {
                if (ev.target.id.startsWith("folder_")) {
                    ev.target.childNodes[2].appendChild(document.getElementById(data));
                } else if (ev.target.nodeName == "LI") {
                    ev.target.parentElement.parentElement.childNodes[2].appendChild(document.getElementById(data));
                }
            } else {
                if (xhttp.responseText.length == 0) {
                    errorMessage.innerHTML = "An unexpected error occurred.";
                } else {
                    errorMessage.innerHTML = xhttp.responseText;
                }
            }
        }
    }
    xhttp.open("POST", `/ria/home/move/${folder_id}/${file_id}`);
    xhttp.send();
}

function removeFile(data) {
    let xhttp = new XMLHttpRequest();

    let file_id = data.replace("file_", "");

    errorMessage.innerHTML = "";
    xhttp.onload = function () {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if (xhttp.status == 200) {
                document.getElementById(data).remove();
            } else {
                if (xhttp.responseText.length == 0) {
                    errorMessage.innerHTML = "An unexpected error occurred.";
                } else {
                    errorMessage.innerHTML = xhttp.responseText;
                }
            }
        }
    }
    xhttp.open("DELETE", `/ria/home/delete/file/${file_id}`);
    xhttp.send();
}

function removeFolder(data) {
    let xhttp = new XMLHttpRequest();

    let folder_id = data.replace("folder_", "");
    folder_id = folder_id.replace("sub", "");

    errorMessage.innerHTML = "";
    xhttp.onload = function () {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if (xhttp.status == 200) {
                if (document.getElementById(data).nodeName == "TD") {
                    document.getElementById(data).parentElement.remove();
                } else {
                    document.getElementById(data).remove();
                }
            } else {
                if (xhttp.responseText.length == 0) {
                    errorMessage.innerHTML = "An unexpected error occurred.";
                } else {
                    errorMessage.innerHTML = xhttp.responseText;
                }
            }
        }
    }
    xhttp.open("DELETE", `/ria/home/delete/folder/${folder_id}`);
    xhttp.send();
}

function createSubfolder() {
    let xhttp = new XMLHttpRequest();

    let name = document.getElementById("modal_subfolder_name").value;
    let select = document.getElementById("modal_subfolder_parent");
    let parent = select.value;
    let date = document.getElementById("modal_subfolder_date").value;

    if (name.length == 0 || date.length == 0) {
        return;
    }

    let params = `name=${name}&parent=${parent}&date=${date}`;

    errorMessage.innerHTML = "";
    xhttp.onload = function () {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if (xhttp.status == 200) {
                home();
            } else {
                if (xhttp.responseText.length == 0) {
                    errorMessage.innerHTML = "An unexpected error occurred.";
                } else {
                    errorMessage.innerHTML = xhttp.responseText;
                }
            }
        }
    }
    xhttp.open("POST", "/ria/home/create/folder/");
    xhttp.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhttp.send(params);
    opened_modal.style.display = "none";
}

function createFolder() {
    let xhttp = new XMLHttpRequest();

    let name = document.getElementById("modal_folder_name").value;
    let date = document.getElementById("modal_folder_date").value;

    if (name.length == 0 || date.length == 0) {
        return;
    }

    let params = `name=${name}&parent=${-1}&date=${date}`;

    errorMessage.innerHTML = "";
    xhttp.onload = function () {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if (xhttp.status == 200) {
                home();
            } else {
                if (xhttp.responseText.length == 0) {
                    errorMessage.innerHTML = "An unexpected error occurred.";
                } else {
                    errorMessage.innerHTML = xhttp.responseText;
                }
            }
        }
    }
    xhttp.open("POST", "/ria/home/create/folder/");
    xhttp.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhttp.send(params);
    opened_modal.style.display = "none";
}

function createSubFolder() {
    let xhttp = new XMLHttpRequest();

    let name = document.getElementById("modal_subfolder_name").value;
    let date = document.getElementById("modal_subfolder_date").value;
    let parent = document.getElementById("modal_subfolder_parent").value;

    if (name.length == 0 || date.length == 0 || parent.length == 0) {
        return;
    }

    let params = `name=${name}&parent=${parent}&date=${date}`;

    errorMessage.innerHTML = "";
    xhttp.onload = function () {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if (xhttp.status == 200) {
                home();
            } else {
                if (xhttp.responseText.length == 0) {
                    errorMessage.innerHTML = "An unexpected error occurred.";
                } else {
                    errorMessage.innerHTML = xhttp.responseText;
                }
            }
        }
    }
    xhttp.open("POST", "/ria/home/create/folder/");
    xhttp.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhttp.send(params);
    opened_modal.style.display = "none";
}

function createFile() {
    let xhttp = new XMLHttpRequest();

    let name = document.getElementById("modal_file_name").value;
    let ext = document.getElementById("modal_file_ext").value;
    let date = document.getElementById("modal_file_date").value;
    let parent = document.getElementById("modal_file_parent").value;
    let type = document.getElementById("modal_file_type").value;
    let summary = document.getElementById("modal_file_summary").value;

    errorMessage.innerHTML = "";
    if (name.length == 0 || ext.length == 0 || date.length == 0 || parent.length == 0 || type.length == 0 || summary.length == 0) {
        return false;
    }

    let params = `name=${name}&ext=${ext}&parent=${parent}&date=${date}&summary=${summary}&type=${type}`;

    xhttp.onload = function () {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if (xhttp.status == 200) {
                home();
            } else {
                if (xhttp.responseText.length == 0) {
                    errorMessage.innerHTML = "An unexpected error occurred.";
                } else {
                    errorMessage.innerHTML = xhttp.responseText;
                }
            }
        }
    }
    xhttp.open("POST", "/ria/home/create/file/");
    xhttp.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhttp.send(params);
    opened_modal.style.display = "none";
}

function logout() {
    localStorage.clear();
    window.location.href = "/ria/logout/";
}

function allowDrop(ev) {
    ev.preventDefault();
}

function drag(ev) {
    ev.dataTransfer.setData("text", ev.target.id);
}

function dropBin(ev) {
    ev.preventDefault();
    showDeleteModal(ev.dataTransfer.getData("text"));
    return true;
}

function dropFolder(ev) {
    ev.preventDefault();
    let data = ev.dataTransfer.getData("text");
    let file_id = data.replace("file_", "");
    let folder_id = ev.target.id.replace("folder_", "");
    if (ev.target.nodeName == "BUTTON" || ev.target.nodeName == "I" || data.includes("folder")) {
        return false;
    }
    if (folder_id.startsWith("file_")) {
        folder_id = ev.target.parentElement.parentElement.id.replace("folder_", "");
    }

    move(folder_id, file_id, ev, data);
}

function showFileModal(subfolder_id, subfolder_name) {
    errorMessage.innerHTML = "";
    opened_modal = document.getElementById("fileModal");
    document.getElementById("modal_file_name").value = "";
    document.getElementById("modal_file_ext").value = "";
    document.getElementById("modal_file_date").value = "";
    document.getElementById("modal_file_parent").value = "";
    document.getElementById("modal_file_type").value = "";
    document.getElementById("modal_file_summary").value = "";
    document.getElementById("modal_file_title").innerHTML = `Create new file in ${subfolder_name}`;
    document.getElementById("modal_file_button").onclick = function () {
        createFile(subfolder_id);
    }
    document.getElementById("modal_file_parent").value = subfolder_id;

    opened_modal.style.display = "block";
}

function showSubfolderModal(parent, name) {
    errorMessage.innerHTML = "";
    document.getElementById("modal_subfolder_date").value = "";
    document.getElementById("modal_subfolder_name").value = "";

    opened_modal = document.getElementById("subfolderModal");
    document.getElementById("modal_subfolder_parent").value = parent;
    document.getElementById("modal_subfolder_title").innerHTML = `Create new folder in ${name}`;

    opened_modal.style.display = "block";
}

function showDeleteModal(data) {
    errorMessage.innerHTML = "";
    opened_modal = document.getElementById("deleteModal");
    document.getElementById("cancelButtonModal").onclick = hideModal;
    if (data.startsWith("file")) {
        document.getElementById("deleteButtonModal").onclick = function () {
            removeFile(data);
            hideModal();
        };
        opened_modal.style.display = "block";
    } else if (data.includes("folder")) {
        document.getElementById("deleteButtonModal").onclick = function () {
            removeFolder(data);
            hideModal();
        };
        opened_modal.style.display = "block";
    }
}

function showFolderModal() {
    errorMessage.innerHTML = "";
    errorMessage.innerHTML = "";
    opened_modal = document.getElementById("folderModal");

    document.getElementById("modal_folder_date").value = "";
    document.getElementById("modal_folder_name").value = "";

    opened_modal.style.display = "block";
}

function showFileDetailsModal(id) {
    errorMessage.innerHTML = "";
    let xhttp = new XMLHttpRequest();

    let title = document.getElementById("modal_file_vis_title");
    let name = document.getElementById("modal_file_vis_name");
    let ext = document.getElementById("modal_file_vis_ext");
    let date = document.getElementById("modal_file_vis_date");
    let summary = document.getElementById("modal_file_vis_summary");
    let type = document.getElementById("modal_file_vis_type");
    let folder = document.getElementById("modal_file_vis_folder");

    errorMessage.innerHTML = "";
    xhttp.onload = function () {
        if (xhttp.readyState == XMLHttpRequest.DONE) {
            if (xhttp.status == 200) {
                json = JSON.parse(this.responseText);
                title.innerHTML = `Details of file ${json["name"]}.${json["ext"]}`;
                name.innerHTML = json["name"];
                ext.innerHTML = json["ext"];
                summary.innerHTML = json["summary"];
                date.innerHTML = json["date"];
                id.innerHTML = json["id"];
                type.innerHTML = json["type"];
                folder.innerHTML = json["folder"];

                opened_modal = document.getElementById("fileVisModal");
                opened_modal.style.display = "block";
            } else {
                if (xhttp.responseText.length == 0) {
                    errorMessage.innerHTML = "An unexpected error occurred.";
                } else {
                    errorMessage.innerHTML = xhttp.responseText;
                }
            }
        }
    }
    xhttp.open("POST", `/ria/home/folder/document/${id}`);
    xhttp.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
    xhttp.send();
}

function hideModal() {
    opened_modal.style.display = "none";
}
