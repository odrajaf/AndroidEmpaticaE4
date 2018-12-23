from flask import render_template
from flask_pymongo import PyMongo
from app import app
from flask import request
import datetime
import time
import gpxpy.geo 
from numbers import Number
from collections import Set, Mapping, deque


app.config["MONGO_URI"] = "mongodb://localhost:27017/empaticaBD"
mongo = PyMongo(app)



@app.route('/', methods=['GET'])
def listarEmail():
	email = request.args.get('email')
	if not (email is None):
		cursorEmail = mongo.db.usuariosEmpatica.find()
		for fila in cursorEmail:
			emailBD = fila.get('email')
			if email == emailBD:
				sesiones = []
				emailElegido = email
				cursorSesiones = mongo.db.sesionesUsuarios.find({'email':email})
				for fila in cursorSesiones:
					objetoFecha = datetime.datetime.strptime(fila['fecha'], "%Y-%m-%d %H:%M:%S.%f")
					fechaFormateada = datetime.datetime.strftime(objetoFecha,'%d/%m/%Y - %H:%M:%S')
					sesiones.append({'idSesion':fila['idSesion'],'fecha': fechaFormateada})
				return render_template('listarEnlaces.html',emailElegido=emailElegido, sesiones=sesiones, tituloSeccion='Sesiones de ',tituloResaltado=emailElegido)
	

	#solo se ejecuta si la primera es falsa
	emails=[]
	cursorEmail = mongo.db.usuariosEmpatica.find()
	for fila in cursorEmail:
		emails.append(fila['email'])
	return render_template('listarEnlaces.html',emails=emails,tituloSeccion='Visualizador, usuarios disponibles:')
	
@app.route('/menuComparar', methods=['GET'])
def menuComparar():
	email = request.args.get('email01')
	email2 = request.args.get('email02')
	if not (email is None):
		if emailCorrecto(email) and emailCorrecto(email2):
				sesiones = []
				emailElegido = email
				cursorSesiones = mongo.db.sesionesUsuarios.find({'email':email})
				for fila in cursorSesiones:
					objetoFecha = datetime.datetime.strptime(fila['fecha'], "%Y-%m-%d %H:%M:%S.%f")
					fechaFormateada = datetime.datetime.strftime(objetoFecha,'%d/%m/%Y - %H:%M:%S')
					sesiones.append({'idSesion':fila['idSesion'],'fecha': fechaFormateada})

				sesiones2 = []
				emailElegido2 = email2
				cursorSesiones = mongo.db.sesionesUsuarios.find({'email':email2})
				for fila in cursorSesiones:
					objetoFecha = datetime.datetime.strptime(fila['fecha'], "%Y-%m-%d %H:%M:%S.%f")
					fechaFormateada = datetime.datetime.strftime(objetoFecha,'%d/%m/%Y - %H:%M:%S')
					sesiones2.append({'idSesion':fila['idSesion'],'fecha': fechaFormateada})
				return render_template('listarComparar.html',emailElegido=emailElegido,emailElegido2=emailElegido2, sesiones=sesiones,sesiones2=sesiones2, tituloSeccion='Seleccione las sesiones a comparar:')
	

	#solo se ejecuta si la primera es falsa
	emails=[]
	cursorEmail = mongo.db.usuariosEmpatica.find()
	for fila in cursorEmail:
		emails.append(fila['email'])
	return render_template('listarComparar.html',emails=emails,tituloSeccion='Escoge a los usuarios a comparar:')

		
	

@app.route('/graficas', methods=['GET'])
def pitarGraficas():
	temperaturas = [];
	pulsacionesMinuto = [];
	EDA = [];
	FuerzaACC = [];
	email = request.args.get('email')
	sesion = request.args.get('sesion')

	if (sesion is None):
		sesion = ""
	
	if (email is None):
		email = ""
	else:
		if emailCorrecto(email):
			datosFormateados = obtenerDatos(email, sesion)
			temperaturas = datosFormateados.get('temperatura')
			pulsacionesMinuto = datosFormateados.get('pulsaciones')
			EDA = datosFormateados.get('EDA')
			FuerzaACC = datosFormateados.get('movimientoAcc')


	return render_template('graficaSimple.html', temperaturas=temperaturas, pulsacionesMinuto=pulsacionesMinuto, EDA=EDA,email=email,FuerzaACC=FuerzaACC,tituloSeccion='Sesion '+sesion+ ' de ',tituloResaltado=email )

@app.route('/comparacion', methods=['GET'])
def comparacion():
	temperaturas = [];
	pulsacionesMinutos = [];
	EDA = [];
	tendenciaPulsaciones = [];
	movimientoAcc = [];
	localizacion = [];

	temperaturasComp = [];
	pulsacionesMinutosComp = [];
	EDAComp = [];
	tendenciaPulsacionesComp = [];
	movimientoAccComp = [];
	localizacionComp = [];

	cercaniaEntreAmbos = [];

	emocion = [];
	emocionComp = [];
	

	cercania = 100 #a menos de 100 metros para que esten cerca
	email = request.args.get('email')
	sesion = request.args.get('sesion')
	emailComp = request.args.get('emailComp')
	sesionComp = request.args.get('sesionComp')
	if emailCorrecto(email):# and  emailCorrecto(emailComp):
		
		datosFormateados = obtenerDatos(email, sesion)
		temperaturas = datosFormateados.get('temperatura')
		pulsacionesMinuto = datosFormateados.get('pulsaciones')
		EDA = datosFormateados.get('EDA')
		tendenciaPulsaciones = datosFormateados.get('tendenciaPulsaciones')
		movimientoAcc = datosFormateados.get('movimientoAcc') 
		localizacion = datosFormateados.get('localizacion') 

		
		datosFormateados = obtenerDatos(emailComp, sesionComp)
		temperaturasComp = datosFormateados.get('temperatura')
		pulsacionesMinutoComp = datosFormateados.get('pulsaciones')
		EDAComp = datosFormateados.get('EDA')
		tendenciaPulsacionesComp = datosFormateados.get('tendenciaPulsaciones')
		movimientoAccComp = datosFormateados.get('movimientoAcc') 
		localizacionComp = datosFormateados.get('localizacion') 
		cercaniaEntreAmbos = geoCernanos(localizacion, localizacionComp,cercania);

		anteriorTendencia = 0;
		emocionValor = 2.5;
		#pasar a la funcion calculo de estimacion
		#ESTIMAR SI LAS PULSACIONES ESTAN ALTAS PORQUE ESTAS CORRIENDO
		for x in range(0,len(EDA)):

			emocionValor = 2.5;
			if (float(EDAComp[x]['eda']) > 1 and float(pulsacionesMinutoComp[x]['pulsaciones']) >= 92):
				if (float(movimientoAccComp[x]['movimientoAcc']) > -0.6):
					emocionValor = 2.5;
				else:
					emocionValor = 2;#extress social

			if (int(anteriorTendencia) == -1 and  int(tendenciaPulsacionesComp[x]['tendenciaPulsaciones']) == 1):
				emocionValor =emocionValor -0.5
			if (float(EDAComp[x]['eda']) > 1 and float(pulsacionesMinutoComp[x]['pulsaciones']) < 92):
				if (float(movimientoAccComp[x]['movimientoAcc']) > -0.6):
					emocionValor = 2.5;
				else:
					emocionValor = 3;#comodo
			anteriorTendencia = tendenciaPulsacionesComp[x]['tendenciaPulsaciones'];


			emocion.append({'fecha':EDA[x]['fecha'],'emocion':emocionValor})

		anteriorTendencia = 0;
		emocionValor = 2.5;
		#ESTIMAR SI LAS PULSACIONES ESTAN ALTAS PORQUE ESTAS CORRIENDO
		for x in range(0,len(EDAComp)):
			emocionValor = 2.5;
			if (float(EDAComp[x]['eda']) > 1 and float(pulsacionesMinutoComp[x]['pulsaciones']) >= 92):
				if (float(movimientoAccComp[x]['movimientoAcc']) > -0.6):
					emocionValor = 2.5;
				else:
					emocionValor = 2;#extress social

			if (int(anteriorTendencia) == -1 and  int(tendenciaPulsacionesComp[x]['tendenciaPulsaciones']) == 1):
				emocionValor =emocionValor -0.5
			if (float(EDAComp[x]['eda']) > 1 and float(pulsacionesMinutoComp[x]['pulsaciones']) < 92):
				if (float(movimientoAccComp[x]['movimientoAcc']) > -0.6):
					emocionValor = 2.5;
				else:
					emocionValor = 3;#comodo
			anteriorTendencia = tendenciaPulsacionesComp[x]['tendenciaPulsaciones'];

			emocionComp.append({'fecha':EDAComp[x]['fecha'],'emocion':emocionValor})	
		

	return render_template('graficaComparativa.html', emocion=emocion,emocionComp=emocionComp,cercania=cercaniaEntreAmbos,email=email,emailComp=emailComp,tituloSeccion=str('Interación '+emailComp+ '-'),tituloResaltado=email )

@app.route('/ultimaInsercion', methods=['GET'])
def ultimaInsercion():
	cursor = mongo.db.datosBiometricos.find().sort("fecha",-1).limit(1)
	datos = ''
	for fila in cursor:
		datos = fila['temperatura'] + ';'+ fila['pulsaciones'] + ';'+ fila['eda'] + ';' +fila['movimientoAcc']
	return datos


@app.route('/presentacion', methods=['GET'])
def presentacion():
	return render_template('presentacion.html')

#vemos si estaban relativamente cerca, debería implementarse más adelante un filtro para comparar a las mismas horas
def geoCernanos( localizacion, localizacionComp,cercania ):
	cercaniaEntreLoc = [];
	for x in range(0,len(localizacion)):
		tamplocalizacionComp = len(localizacionComp)
		if tamplocalizacionComp > x:
			lon = localizacion[x]['localizacion']['coordenada'].get('longitude')
			lat = localizacion[x]['localizacion']['coordenada'].get('latitude')
			lon2 = localizacionComp[x]['localizacion']['coordenada'].get('longitude')
			lat2 = localizacionComp[x]['localizacion']['coordenada'].get('latitude')
			distancia = gpxpy.geo.haversine_distance(lat, lon, lat2, lon2) 
			if distancia > cercania: 
				cercaniaEntreLoc.append({'fecha':localizacionComp[x]['fecha'],'cercania':0})
			else:
				cercaniaEntreLoc.append({'fecha':localizacionComp[x]['fecha'],'cercania':1})
	return cercaniaEntreLoc

#comprobar que el email enviado esta en la base de datos
def emailCorrecto( emailEnviado ):
	cursorEmail = mongo.db.usuariosEmpatica.find()
	for fila in cursorEmail:		
		if emailEnviado == fila.get('email'):
			return True
	return False

#Obtenemos los datos de la base de datos de la sesion y email concreto
def obtenerDatos(email, sesion):
	temperaturas = [];
	pulsacionesMinuto = [];
	EDA = [];
	tendenciaPulsaciones = [];
	movimientoAcc= [];
	localizacion = [];

	cursor = mongo.db.datosBiometricos.find({'email':email,'idSesion':sesion})
	for fila in cursor:
		fechaMilisegundos = str(int(time.mktime(datetime.datetime.strptime(fila['fecha'], "%Y-%m-%d %H:%M:%S.%f").timetuple()))) + fila['fecha'][20:23]
		temperaturas.append({'fecha':fechaMilisegundos,'temperatura':fila['temperatura']})
		pulsacionesMinuto.append({'fecha':fechaMilisegundos,'pulsaciones':fila['pulsaciones']})
		EDA.append({'fecha':fechaMilisegundos,'eda':fila['eda']})
		tendenciaPulsaciones.append({'fecha':fechaMilisegundos,'tendenciaPulsaciones':fila['tendenciaPulsaciones']})
		movimientoAcc.append({'fecha':fechaMilisegundos,'movimientoAcc':fila['movimientoAcc']})
		localizacion.append({'fecha':fechaMilisegundos,'localizacion':fila['localizacion']})


	return  {'temperatura':temperaturas,'pulsaciones':pulsacionesMinuto,'EDA':EDA,'tendenciaPulsaciones':tendenciaPulsaciones,'movimientoAcc':movimientoAcc,'localizacion':localizacion}

	 


