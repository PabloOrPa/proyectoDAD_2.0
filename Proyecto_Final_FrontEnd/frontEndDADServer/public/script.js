const apiBaseUrl = 'http://192.168.169.35:8084/api';

$(document).ready(function () {
    // Check if user is logged in
    const username = localStorage.getItem('username');
    if (username) {
        showDashboard(username);
    } else {
        showLogin();
    }

    // Asignar manejadores de eventos al documento en lugar de a elementos específicos
    $(document).on('click', '#login-btn', handleLogin);                 // Maneja el login. Si es satisfactorio -> Muestra el Dashboard
    $(document).on('click', '#register-btn', handleRegister);           // Maneja el register
    $(document).on('click', '#logout-btn', handleLogout);               // Maneja el cierre de sesión
    $(document).on('click', '#show-register', showRegister);            // Muestra la interfaz de Registro
    $(document).on('click', '#show-login', showLogin);                  // Muestra la interfaz de Login
    $(document).on('click', '#register-group-btn', handleRegisterGroup);// Registra un grupo domótico a nuestro nombre
    $(document).on('focus', '#grupos-del-user', handleGruposDelUser);// Este evento ha de ser "focus", ya que el "click" resetea las opciones del menú cuando se abre pero también cuando se selecciona un elemento
    $(document).on('click', '#boton-estado', handleEstado);                 // Muestra el estado de sensores y actuadores
    $(document).on('click', '#boton-historico-temp', handleHistoricoTemp);  // Muestra un gráfico con la evolución de la temperatura
    $(document).on('click', '#boton-historico-luz', handleHistoricoLuz);    // Muestra un gráfico con la evolución de la luminosidad
    $(document).on('click', '#show-manualOverride', handleManualOverride);  // Muestra la interfaz de control manual
    $(document).on('click', '#boton-manualOverride-desactivado', activaManualOverride); // Activa el control manual
    $(document).on('click', '#boton-manualOverride-activado', desactivaManualOverride); // Desactiva el control manual
    $(document).on('click', '#boton-lucesON', enciendeLuces);               // Enciende las luces de manera manual
    $(document).on('click', '#boton-lucesOFF', apagaLuces);                 // Apaga las luces de manera manual
    $(document).on('click', '#boton-ventiladoresON', enciendeVentiladores); // Enciende los ventiladores de manera manual
    $(document).on('click', '#boton-ventiladoresOFF', apagaVentiladores);   // Apaga los ventiladores de manera manual
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

async function handleManualOverride(){
    // En esta función cambiamos el contenido del display por un cuadro de mandos
    const idGroup = document.getElementById('grupos-del-user').value;
    let manualOverride = null;
    

    // Vemos el estado del manualOverride en la Base de datos
    // /groupUser/estadoMO/35
    await $.ajax({
        url: `${apiBaseUrl}/groupUser/estadoMO/${idGroup}`,
        method: 'GET',
        contentType: 'application/json',
        success: function (response) {
            manualOverride = response;
        }
    });
   
    

    if(manualOverride){
        activaManualOverride();
    }else{
        desactivaManualOverride();
    }
    // Creamos el botón en consecuencia
    
    

}

function activaManualOverride(){
    // En esta función Comunicamos en la API qué queremos hacer con los actuadores
    const idGroup = document.getElementById('grupos-del-user').value;
    const display = document.getElementById('display');
    
    let grafico = parseHTML('<div class="row justify-content-center" style="margin:5%"><button id="boton-manualOverride-activado" class="btn btn-success">Desactiva Control Manual</button></div>');
    
    let manualOverride = true;

    $.ajax({
        url: `${apiBaseUrl}/groupUser`, // URL del recurso que deseas actualizar
        method: 'PUT', // Método HTTP cambiado a PUT
        contentType: 'application/json', // Tipo de contenido
        data: JSON.stringify({ manualOverride, idGroup }), // Datos a enviar en formato JSON
        success: function (response) {
            display.innerHTML = '';
            display.appendChild(grafico);
            display.append(parseHTML('<div id="botones-control-manual-luces" class="row justify-content-center"></div>'));
            display.append(parseHTML('<div id="botones-control-manual-ventiladores" class="row justify-content-center"></div>'));
            addBotones();
        },
        error: function (xhr) {
            alert("Hubo un error: " + xhr.responseText);
        }
    });
    
}
async function addBotones(){
    const idGroup = document.getElementById('grupos-del-user').value;
    const botonesL = document.getElementById('botones-control-manual-luces');
    const botonesV = document.getElementById('botones-control-manual-ventiladores');
    let bombillas = false;
    let ventiladores = false;

    await $.ajax({
        url: `${apiBaseUrl}/reles/estado/${idGroup}`,
        method: 'GET',
        contentType: 'application/json',
        success: function (response) {

            response.forEach(function (item) {
                if(item.tipo=="Bombilla"){
                    bombillas=true;
                }else if(item.tipo=="Ventilador"){
                    ventiladores=true;
                }
            });

        }
    });

    if(bombillas){
        botonesL.appendChild(parseHTML('<button id="boton-lucesON" class="btn btn-warning" style="margin:2%; padding:1%"> Encender luces </button>'));
        botonesL.appendChild(parseHTML('<button id="boton-lucesOFF" class="btn btn-dark" style="margin:2%; padding:1%"> Apagar luces</button>'));
    }

    if(ventiladores){
        botonesV.appendChild(parseHTML('<button id="boton-ventiladoresON" class="btn btn-warning" style="margin:2%; padding:1%"> Encender ventiladores </button>'));
        botonesV.appendChild(parseHTML('<button id="boton-ventiladoresOFF" class="btn btn-dark" style="margin:2%; padding:1%"> Apagar ventiladores </button>'));
    }

}

function enciendeLuces(){
    const idGroup = document.getElementById('grupos-del-user').value;
    enciendeApagaManual(true, "Bombilla",idGroup);
}

function apagaLuces(){
    const idGroup = document.getElementById('grupos-del-user').value;
    enciendeApagaManual(false, "Bombilla",idGroup);
}

function enciendeVentiladores(){
    const idGroup = document.getElementById('grupos-del-user').value;
    enciendeApagaManual(true, "Ventilador",idGroup);
}

function apagaVentiladores(){
    const idGroup = document.getElementById('grupos-del-user').value;
    enciendeApagaManual(false, "Ventilador",idGroup);
}

function enciendeApagaManual(estado, tipo, idGroup){
    $.ajax({
        url: `${apiBaseUrl}/reles/manual`,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ estado, tipo, idGroup }),
        success: function (response) {
            $('#user-group-error').text("Exito en la petición").removeClass('hidden')
        },
        error: function (xhr) {
            alert(xhr.responseText);
        }
    });
}

function desactivaManualOverride(){
    // En esta función Comunicamos en la API qué queremos hacer con los actuadores
    const idGroup = document.getElementById('grupos-del-user').value;
    const display = document.getElementById('display');
    let grafico = parseHTML('<div class="row justify-content-center"><button id="boton-manualOverride-desactivado" class="row btn btn-danger">Activa Control manual</button></div>');

    let manualOverride = false;

    $.ajax({
        url: `${apiBaseUrl}/groupUser`, // URL del recurso que deseas actualizar
        method: 'PUT', // Método HTTP cambiado a PUT
        contentType: 'application/json', // Tipo de contenido
        data: JSON.stringify({ manualOverride, idGroup }), // Datos a enviar en formato JSON
        success: function (response) {
            display.innerHTML = '';
            display.appendChild(grafico);
        },
        error: function (xhr) {
            alert("Hubo un error: " + xhr.responseText);
        }
    });
}



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

            response.forEach(function (item) {
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
            localStorage.setItem('username', username);
            showDashboard(username);
        },
        error: function (xhr) {
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
        data: JSON.stringify({ username, password: hashedPassword }),
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



// Cambios de vista




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
                    <div class="row justify-content-center"><button id="show-manualOverride" class="btn btn-success" style="margin:3%; margin-bottom:20%; font-size:200%";">Control manual</button></div>
                    
                    
                    
                    
                </div>
                
                <div id="display" class = "col-md-8 justify-content-center">
                    <img src="images/placeholder.jpg" alt="Placeholder" class="img-fluid">
                </div>



            </div>

        </div>

    `);







}
