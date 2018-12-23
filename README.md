# AndroidEmpaticaE4

![AndroidEmpaticaE4](https://github.com/odrajaf/AndroidEmpaticaE4/blob/master/imagenPortada.png)

El proyecto consiste en una aplicación *android* que se sincroniza mediante bluetooth a la pulsera Empatica E4,
captando los datos de la misma y concinando los datos que después se enviarán a un servidor.
[AndroidEmpaticaE4](https://github.com/odrajaf/AndroidEmpaticaE4/tree/master/AndroidEmpaticaE4)

La recepción de datos se hace a traves de una inferfaz *RESTful* en *nodeJS* que se encargará de la recepción de los 
datos insertandolos en la base de datos NOSQL *mongoDB*
[Inferfaz RESTful en nodeJS](https://github.com/odrajaf/AndroidEmpaticaE4/tree/master/InterfazRestFulnodejs)

Además contará con una interfaz web implementada usando el microframework *flask* de *python* para mostrar los datos, además 
de hacer uso de la librería en javascript de *canvasJS* para el pintado de los datos.
[Inferfaz Web](https://github.com/odrajaf/AndroidEmpaticaE4/tree/master/servidorFlaskPython)

Para más detalles se cuenta con la siguiente documentación:
[Trabajo Fin de Grado - Eloy Fajardo Sánchez (v3)](https://github.com/odrajaf/AndroidEmpaticaE4/blob/master/Trabajo%20Fin%20de%20Grado%20-%20Eloy%20Fajardo%20S%C3%A1nchez%20(v3).pdf)
