const apiBaseUrl = 'http://localhost:8084/api';

$(document).ready(function () {
    // Check if user is logged in
    const username = localStorage.getItem('username');
    if (username) {
        showDashboard(username);
    } else {
        showLogin();
    }

    // Asignar manejadores de eventos al documento en lugar de a elementos específicos
    $(document).on('click', '#login-btn', handleLogin);
    $(document).on('click', '#register-btn', handleRegister);
    $(document).on('click', '#logout-btn', handleLogout);
    $(document).on('click', '#show-register', showRegister);
    $(document).on('click', '#show-login', showLogin);
    $(document).on('click', '#register-group-btn', handleRegisterGroup);
    $(document).on('focus', '#grupos-del-user', handleGruposDelUser);
    $(document).on('click', '#boton-estado', handleEstado);
    $(document).on('click', '#boton-historico-temp', handleHistoricoTemp);
    $(document).on('click', '#boton-historico-luz', handleHistoricoLuz);
});







// Funciones de generación de gráficos:

function graficoTemp(datos) {

    const valores = [];
    const etiquetas = [];
    const paso = (datos.length / 10).toFixed(0);

    let cuenta = 0;
    for (let dato of datos) {
        valores.push(dato.valor);
        etiquetas.push(new Date(dato.timestamp).toLocaleString());
    }

    var ctx = document.getElementById('grafico').getContext('2d')

    var graficaTemp = new Chart(ctx, {
        type: 'line',
        data: {
            labels: etiquetas,
            datasets: [{
                label: 'Temperatura en ºC',
                data: valores, 
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 2,
                fill: false
            }]
        },
        options: {
            scales: {
                x: {
                    ticks: {
                        callback: function (value, index, ticks) {
                            return index % paso === 0 ? etiquetas[index / paso] : '';
                        }
                    }
                },
                y: {
                    beginAtZero: true
                }
            }
        }
    });


}



function graficoLuz(datos) {
    
    const valores = [];
    const etiquetas = [];
    const paso = (datos.length / 10).toFixed(0);

    let cuenta = 0;
    for (let dato of datos) {
        valores.push(dato.valor/4096*100);
        etiquetas.push(new Date(dato.timestamp).toLocaleString());
    }


    var ctx = document.getElementById('grafico').getContext('2d')

    var graficaTemp = new Chart(ctx, {
        type: 'line',
        data: {
            labels: etiquetas, 
            datasets: [{
                label: '% de luminosidad',
                data: valores, 
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 2,
                fill: false
            }]
        },
        options: {
            scales: {
                x: {
                    ticks: {
                        callback: function (value, index, ticks) {
                            return index % paso === 0 ? etiquetas[index / paso] : '';
                        }
                    }
                },
                y: {
                    beginAtZero: true
                }
            }
        }
    });


}




















// Función auxiliar directamente robada de utils.js de IISSI2
function parseHTML(str) {
    let tmp = document.implementation.createHTMLDocument();
    tmp.body.innerHTML = str;
    return tmp.body.children[0];
}
// Funciones para mostrar sensores y actuadores por pantalla como "card":
function asCardSensorTemp(sensor) {
    let valorTruncado = sensor.valor.toFixed(2);
    let html = `
    <div class="col-md-4">
        <div class="card bg-secondary text-light">
            <h1 style="margin-left:30%">Temperatura: ${valorTruncado} ºC</h1>

            <div class="card-body">
                <h5 class="card-title text-center">Sensor: ${sensor.idTemp}</h5>
                <p class="card-text">Placa: ${sensor.idPlaca}</p>
            </div>

        </div>
    </div>`;
    let card = parseHTML(html);
    return card;
}

function asCardSensorLuz(sensor) {
    let valorPorcentuado = (sensor.valor / 4096 * 100).toFixed(2);
    let html = `
    <div class="col-md-4">
        <div class="card bg-secondary text-light">
            <h1 style="margin-left:30%">Luminosidad: ${valorPorcentuado} %</h1>

            <div class="card-body">
                <h5 class="card-title text-center">Sensor: ${sensor.idFotoRes}</h5>
                <p class="card-text">Placa: ${sensor.idPlaca}</p>
            </div>

        </div>
    </div>`;
    let card = parseHTML(html);
    return card;
}

function asCardRele(rele) {
    let imagenUrl = "";
    if (rele.tipo == "Bombilla") {
        if (rele.estado == true) {
            imagenUrl = "./images/BombillaON.png";
        } else if (rele.estado == false) {
            imagenUrl = "./images/BombillaOFF.jpg";
        }
    } else if (rele.tipo == "Ventilador") {
        if (rele.estado == true) {
            imagenUrl = "./images/VentiladorON.gif";
        } else if (rele.estado == false) {
            imagenUrl = "./images/VentiladorOFF.png";
        }
    } else {
        console.log("No entendí ni verga");
    }


    let html = `
    <div class="col-md-4">
        <div class="card bg-secondary text-light">
            <img src="${imagenUrl}" class="card-img-top" style="max-width:200px; max-height:200px; margin-left:25%">

            <div class="card-body">
                <h5 class="card-title text-center">Sensor: ${rele.idRele}</h5>
                <p class="card-text">Placa: ${rele.idPlaca}</p>
            </div>

        </div>
    </div>`;
    let card = parseHTML(html);
    return card;
}


// Manejadores de eventos: 

function handleHistoricoLuz(){
    const idGroup = document.getElementById('grupos-del-user').value;

    const display = document.getElementById('display');
    display.innerHTML = '';

    let grafico = parseHTML('<canvas id="grafico" style="max-height:600px"></canvas>');
    display.appendChild(grafico);

    $.ajax({
        url: `${apiBaseUrl}/sLuz/historico/${idGroup}`,
        method: 'GET',
        contentType: 'application/json',
        success: function (response) {

            graficoLuz(response);

        }
    });
    
    
}

function handleHistoricoTemp() {
    const idGroup = document.getElementById('grupos-del-user').value;

    const display = document.getElementById('display');
    display.innerHTML = '';

    let grafico = parseHTML('<canvas id="grafico" style="max-height:600px"></canvas>');
    display.appendChild(grafico);

    $.ajax({
        url: `${apiBaseUrl}/sTemp/historico/${idGroup}`,
        method: 'GET',
        contentType: 'application/json',
        success: function (response) {

            graficoTemp(response);

        }
    });

}


function handleEstado() {
    const idGroup = document.getElementById('grupos-del-user').value;
    console.log(idGroup);

    const display = document.getElementById('display');
    display.innerHTML = '';


    // Fila de sensores
    let rowSensores = parseHTML('<div id="Sensores" class="row"></div>');
    display.appendChild(rowSensores);


    // GET al estado de los sensores de temperatura
    $.ajax({
        url: `${apiBaseUrl}/sTemp/estado/${idGroup}`,
        method: 'GET',
        contentType: 'application/json',
        success: function (response) {

            response.forEach(function (item) {
                let card = asCardSensorTemp(item);
                rowSensores.appendChild(card);
            });

        }
    });

    // GET al estado de los sensores de Luz
    $.ajax({
        url: `${apiBaseUrl}/sLuz/estado/${idGroup}`,
        method: 'GET',
        contentType: 'application/json',
        success: function (response) {

            response.forEach(function (item) {
                let card = asCardSensorLuz(item);
                rowSensores.appendChild(card);
            });

        }
    });

    display.appendChild(parseHTML('<br>'));

    // Fila de actuadores
    let rowActuadores = parseHTML('<div id="Actuadores" class="row"></div>');
    display.appendChild(rowActuadores);

    // GET al estado de los actuadores
    $.ajax({
        url: `${apiBaseUrl}/reles/estado/${idGroup}`,
        method: 'GET',
        contentType: 'application/json',
        //data: {idGroup:idGroup},
        success: function (response) {

            console.log(response);

            response.forEach(function (item) {
                console.log(item.tipo + " " + item.idRele + ": " + item.estado);
                let card = asCardRele(item);
                rowActuadores.appendChild(card);
            });

        }
    });


}

function handleGruposDelUser() {
    const username = localStorage.getItem('username');

    $.ajax({
        url: `${apiBaseUrl}/groupUser`,
        method: 'GET',
        contentType: 'application/json',
        data: { username: username },
        success: function (response) {
            const gruposSelect = $('#grupos-del-user');

            const grupos = JSON.parse(response);
            console.log(response);
            // Vaciar el select actual
            gruposSelect.empty();

            // Asumimos que response es una lista de enteros
            grupos.forEach(function (grupo) {
                const newOption = $('<option>', {
                    value: grupo,
                    text: `Grupo ${grupo}`
                });
                gruposSelect.append(newOption);
            });

        }
    });
}

function handleRegisterGroup() {
    const username = localStorage.getItem('username');
    const idGroup = $('#input-idGroup').val()

    $.ajax({
        url: `${apiBaseUrl}/groupUser`,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ username, idGroup }),
        success: function (response) {
            $('#user-group-error').text("Grupo registrado con exito").removeClass('hidden')
        },
        error: function (xhr) {
            $('#user-group-error').text(xhr.responseText).removeClass('hidden');
        }
    });

}

function handleLogin() {
    const username = $('#login-username').val();
    const password = $('#login-password').val();
    const hashedPassword = CryptoJS.SHA256(password).toString();

    $.ajax({
        url: `${apiBaseUrl}/login`,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ username, password: hashedPassword }),
        success: function (response) {
            alert("correcto");
            localStorage.setItem('username', username);
            showDashboard(username);
        },
        error: function (xhr) {
            alert(xhr.responseText);
            $('#login-error').text(xhr.responseText).removeClass('hidden');
        }
    });
}

function handleRegister() {
    const username = $('#register-username').val();
    const password = $('#register-password').val();
    const confirmPassword = $('#register-confirm-password').val();

    if (password !== confirmPassword) {
        $('#register-error').text('Las contraseñas no coinciden').removeClass('hidden');
        return;
    }

    const hashedPassword = CryptoJS.SHA256(password).toString();

    $.ajax({
        url: `${apiBaseUrl}/register`,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(requestData),
        success: function () {
            showLogin();
        },
        error: function (xhr) {
            $('#register-error').text(xhr.responseText).removeClass('hidden');
        }
    });
}

function handleLogout() {
    localStorage.removeItem('username');
    showLogin();
}

function showLogin() {
    $('#content').html(`
        <div id="login-view" class="form-container col-md-4">
            <h3>Inicie sesión</h3>
            <div id="login-error" class="error hidden"></div>
            <div class="form-group">
                <label for="login-username">Username</label>
                <input type="text" class="form-control" id="login-username" required>
            </div>
            <div class="form-group">
                <label for="login-password">Password</label>
                <input type="password" class="form-control" id="login-password" required>
            </div>
            <button id="login-btn" class="btn btn-primary">Iniciar sesión</button>
            <p class="mt-2">¿No tienes cuenta? <a href="#" id="show-register">Regístrate aquí</a></p>
        </div>
    `);
}

function showRegister() {
    $('#content').html(`
        <div id="register-view" class="form-container col-md-4">
            <h3>Regístrate aquí</h3>
            <div id="register-error" class="error hidden"></div>
            <div class="form-group">
                <label for="register-username">Username</label>
                <input type="text" class="form-control" id="register-username" required>
            </div>
            <div class="form-group">
                <label for="register-password">Password</label>
                <input type="password" class="form-control" id="register-password" required>
            </div>
            <div class="form-group">
                <label for="register-confirm-password">Confirmar Password</label>
                <input type="password" class="form-control" id="register-confirm-password" required>
            </div>
            <button id="register-btn" class="btn btn-primary">Registrarse</button>
            <p class="mt-2">¿Ya tienes cuenta? <a href="#" id="show-login">Volver para iniciar sesión</a></p>
        </div>
    `);
}

function showDashboard(username) {

    /*
    Ante todo, pido perdón por la aberración de código que se va a presenciar a continuación.
    Hay parches estéticos por todos lados, pero prefiero centrarme en lo importante y que quede simplemente medio bonito
    */
    $('#content').html(`
        <div id="dashboard-view" class="container-fluid">
            
            <div id="cabecera-dashboard" class="row">
                
                <div class="col-md-3">
                    <h3>Bienvenid@ <span id="username-display">${username}</span></h3>
                </div>
                
                <div class="form-group col-md-7" style="display:flex; flex-wrap: wrap;">
                    <label for="input-idGroup" style="flex:2;padding-right: 10px;text-align: right ;">Registra un grupo nuevo:</label>
                    <input type="number" class="form-control d-flex justify-content-left" id="input-idGroup" style="flex:1;padding-right: 10px;" required>
                    <div style="flex:0.2"></div>
                    <button id="register-group-btn" class="btn btn-success" style="flex:1;">Registrar</button>
                    <div style="flex:2"><div id="user-group-error" class="error hidden" style="margin-left:20px"></div></div>
                    <div style="flex:3"></div>
                </div>

                <div class="col-md-2 d-flex justify-content-end">
                    <button id="logout-btn" class="btn btn-danger">Logout</button>
                </div>
            </div>  





            <div class="row justify-content-left align-items-center" style="margin-top:40px">
                
                <div id="botones-accion-dashboard" class="col-md-4 ">
                    <div id="group-select" class="row justify-content-start" style="margin-left:2%; margin-bottom:15%;" >
                        <label for="grupos-del-user" class="form-label"style="margin-right:20px">Selecciona el grupo:</label>
                        <select id="grupos-del-user" class="form-select">
                        <option value="-1">Grupo - </option>
                        </select>
                    </div>  
                    <div class="row justify-content-center"><button id="boton-estado" class="btn btn-info" style="margin:3%; font-size:200%;">Consultar estado</button></div>
                    <div class="row justify-content-center"><button id="boton-historico-temp" class="btn btn-success" style="margin:3%; font-size:200%";>Histórico Temperatura</button></div>
                    <div class="row justify-content-center"><button id="boton-historico-luz" class="btn btn-success" style="margin:3%; font-size:200%";">Histórico Luz</button></div>
                    <div class="row justify-content-center"><button id="ejemplo" class="btn btn-success" style="margin:3%; margin-bottom:20%; font-size:200%";">Ejemplo</button></div>
                    
                    
                    
                    
                </div>
                
                <div id="display" class = "col-md-8 justify-content-center">
                    <img src="images/placeholder.jpg" alt="Placeholder" class="img-fluid">
                </div>



            </div>

        </div>

    `);







}
