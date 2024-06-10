const express = require('express');
const cors = require('cors');
const app = express();
const path = require('path');

const PORT = process.env.PORT || 8087;
const HOST = '192.168.169.35';

app.use(cors());// Para permitir acceso a la api desde cualquier origen

app.use(express.static(path.join(__dirname, 'public')));

app.listen(PORT, HOST, () => {
    console.log(`Servidor corriendo en http://${HOST}:${PORT}`);
});

