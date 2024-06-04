package rest;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import sensores_actuadores.ActuadorRele;
import sensores_actuadores.ActuadorReleListWrapper;
import sensores_actuadores.SensorLuz;
import sensores_actuadores.SensorTemperatura;


public class RestServer extends AbstractVerticle {
	
	// Declaramos el cliente SQL
	private MySQLPool mySqlClient;
	
	private Gson gson;
	
	public void start(Promise<Void> startFuture) {
		
		// Instanciamos Gson
		gson = new GsonBuilder().setDateFormat("dd-MM-yyyy").create();
		
		// Definimos el objeto router:
		Router router = Router.router(vertx);

		// Para la conexión a la base de datos:
		
		MySQLConnectOptions connectOptions = new MySQLConnectOptions().setPort(3306).setHost("localhost")
				.setDatabase("domoticaDAD").setUser("dad").setPassword("pwddad");
		
		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
		
		mySqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);
		
		
		// Tratamos los posibles resultados del despliegue del server:
		vertx.createHttpServer().requestHandler(router::handle).listen(8084, "0.0.0.0", result -> {
			if(result.succeeded())startFuture.complete();
			else startFuture.fail(result.cause());
		});
		

		// API relés:
		router.route("/api/reles*").handler(BodyHandler.create());		// Crea la "ruta"
		router.get("/api/reles/todos").handler(this::getAllReles);			// Devuelve todas las entradas
		router.get("/api/reles/all/:idRele").handler(this::getAllReleWithId);		// Entradas de un relé concreto (filtra por Id)
		router.get("/api/reles/last/:idRele").handler(this::getLastReleWithId);
		router.get("/api/reles/estado/:idGroup").handler(this::getAllRelesFromGroup);
		router.post("/api/reles").handler(this::addOneRele);
		
		// API sensoresLuz:
		router.route("/api/sLuz*").handler(BodyHandler.create());
		router.get("/api/sLuz/todos").handler(this::getAllSLuz);
		router.get("/api/sLuz/all/:idSLuz").handler(this::getAllSLuzWithId);
		router.get("/api/sLuz/last/:idSLuz").handler(this::getLastSLuzWithId);
		router.get("/api/sLuz/estado/:idGroup").handler(this::getAllSLuzFromGroup);
		router.post("/api/sLuz").handler(this::addOneSLuz);
		
		// API sensoresTemp:
		router.route("/api/sTemp").handler(BodyHandler.create());
		router.get("/api/sTemp/todos").handler(this::getAllSTemp);
		router.get("/api/sTemp/all/:idSTemp").handler(this::getAllSTempWithId);
		router.get("/api/sTemp/last/:idSTemp").handler(this::getLastSTempWithId);
		router.get("/api/sTemp/estado/:idGroup").handler(this::getAllSTempFromGroup);
		router.post("/api/sTemp/").handler(this::addOneSTemp);
		
	}
	
	
	// Funciones de la API con BBDD
	
	// reles
	
	private void getAllReles(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			if(connection.succeeded()) {
				connection.result()
				.preparedQuery("SELECT * FROM domoticadad.reles;")
				.execute(res -> {
					if(res.succeeded()) {
											
						List<ActuadorRele> releList = new ArrayList<>();

						res.result().forEach(row -> {
							ActuadorRele ar = new ActuadorRele(row.getInteger("idRele"), 
									row.getBoolean("estado"),
									row.getLong("tStamp"),
									row.getInteger("idPlaca"),
									row.getInteger("idGroup"),
									row.getInteger("id"));
							releList.add(ar);
						});
						routingContext.response()
						.putHeader("content-type", "application/json")
                        .end(gson.toJson(releList));
						
					}else {
						routingContext.response().setStatusCode(500).end("Database error: " + res.cause().getMessage());
					}
					connection.result().close();// Importante cerrar siempre la conexion
				});
			}else {
				routingContext.response().setStatusCode(500).end("Connection error: " + connection.cause().getMessage());
			}
		});
	}
	
	private void getAllReleWithId(RoutingContext routingContext) {
		
		mySqlClient.getConnection(connection -> {
			if(connection.succeeded()) {
				// Si se establece conexión, extraigo el id del Rele que me interesa para la consulta:
				Integer idRele = Integer.valueOf(routingContext.request().getParam("idRele"));
				
				connection.result()
				.preparedQuery("SELECT * FROM domoticadad.reles WHERE idRele = ? ")
				.execute(Tuple.of(idRele), res -> {
					if(res.succeeded()) {
						// Si se hace la consulta correctamente, creo una lista en la que almaceno las entradas del rele
						List<ActuadorRele> releList = new ArrayList<>();
						
						res.result().forEach(row -> {
							ActuadorRele ar = new ActuadorRele(row.getInteger("idRele"), 
									row.getBoolean("estado"),
									row.getLong("tStamp"),
									row.getInteger("idPlaca"),
									row.getInteger("idGroup"),
									row.getInteger("id"));
							releList.add(ar);
						});
						routingContext.response()
						.putHeader("content-type", "application/json")
                        .end(gson.toJson(releList));
					}else {
						routingContext.response().setStatusCode(500).end("Database error: " + res.cause().getMessage());
					}
					connection.result().close();
				});
				
			}else {
				routingContext.response().setStatusCode(500).end("Connection error: " + connection.cause().getMessage());
			}
		});
	}
	
	private void getLastReleWithId(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			if(connection.succeeded()) {
				Integer idRele = Integer.valueOf(routingContext.request().getParam("idRele"));
				
				connection.result()
				.preparedQuery("SELECT * FROM domoticadad.reles WHERE idRele = ? ORDER BY tStamp DESC LIMIT 1")
				.execute(Tuple.of(idRele), res -> {
					if(res.succeeded()) {
						
						// res.result() da un Set<row>. Como en este caso solo va a tener una fila, extraemos el "next" 
						// del iterador, evitando bucles o cosas raras que funcionan en otras consultas pero enturbiarian esta
						Row row = res.result().iterator().next();
						
						ActuadorRele ar = new ActuadorRele(row.getInteger("idRele"), 
								row.getBoolean("estado"),
								row.getLong("tStamp"),
								row.getInteger("idPlaca"),
								row.getInteger("idGroup"),
								row.getInteger("id"));
						
						routingContext.response()
						.putHeader("content-type", "application/json")
                        .end(gson.toJson(ar));
						
					}else {
						routingContext.response().setStatusCode(500).end("Database error: " + res.cause().getMessage());
					}
					connection.result().close();
				});
				
			}else {
				routingContext.response().setStatusCode(500).end("Connection error: " + connection.cause().getMessage());
			}
		});
	}
	
	private void getAllRelesFromGroup(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			if(connection.succeeded()) {
				
				Integer idGroup = Integer.valueOf(routingContext.request().getParam("idGroup"));
				
				connection.result()
				// Esta query es algo compleja de entender:
				// 1º - la subconsulta interior al inner join nos facilita una tabla con los pares: idRele - maxTimestamp
				// 2º - Se une dicha "tabla" con los datos de la tabla completa, dandonos así todos los valores
				// He de admitir que ChatGPT ha sido un gran consejero en cuanto al diseño de esta consulta se refiere
				.preparedQuery("SELECT r1.*"
						+ " FROM domoticadad.reles r1 "
						+ " INNER JOIN ("
						+ "    SELECT idRele, MAX(tStamp) AS maxTimestamp"
						+ "    FROM domoticadad.reles"
						+ "    WHERE idGroup = ?"
						+ "    GROUP BY idRele"
						+ ") r2 ON r1.idRele = r2.idRele AND r1.tStamp = r2.maxTimestamp"
						+ " WHERE r1.idGroup = ?")
				.execute(Tuple.of(idGroup, idGroup), res -> {
					if(res.succeeded()) {
						List<ActuadorRele> releList = new ArrayList<>();
						
						res.result().forEach(row -> {
							ActuadorRele ar = new ActuadorRele(row.getInteger("idRele"), 
									row.getBoolean("estado"),
									row.getLong("tStamp"),
									row.getInteger("idPlaca"),
									row.getInteger("idGroup"),
									row.getInteger("id"));
							releList.add(ar);
						});
						routingContext.response()
						.putHeader("content-type", "application/json")
                        .end(gson.toJson(releList));
					}else {
						routingContext.response().setStatusCode(500).end("Database error: " + res.cause().getMessage());
					}
					connection.result().close();
				});
				
			}else {
				routingContext.response().setStatusCode(500).end("Connection error: " + connection.cause().getMessage());
			}
		});
	}
	
	private void addOneRele(RoutingContext routingContext) {
		
		
		// 1º - Extraemos los parámetros:
		JsonObject body = routingContext.body().asJsonObject();
		
		Integer idRele = body.getInteger("idRele");
		Boolean estado = body.getBoolean("estado");
		Long tStamp = System.currentTimeMillis();
		Integer idPlaca = body.getInteger("idPlaca");
		Integer idGrupo = body.getInteger("idGroup");
		
		// 2º - Intentamos el POST
		mySqlClient.getConnection(connection ->{
			if(connection.succeeded()) {
				connection.result()
				.preparedQuery("INSERT INTO domoticadad.reles (idRele, estado, tStamp, idPlaca, idGroup) "
						+ "VALUES (?,?,?,?,?)").execute(Tuple.of(idRele, estado, tStamp, idPlaca, idGrupo),
						res ->{
							if(res.succeeded()) {
								routingContext.response().setStatusCode(201).end("Rele añadido correctamente");
							}else {
								routingContext.response().setStatusCode(500).end("Database error: " + res.cause().getMessage());
							}
						connection.result().close();	
						});
			}else {
				routingContext.response().setStatusCode(500).end("Connection error: " + connection.cause().getMessage());
			}
		});
	}
	
	
	
	// sensores Luz
	
	private void getAllSLuz(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			if(connection.succeeded()) {
				connection.result()
				.preparedQuery("SELECT * FROM domoticadad.sLuz;")
				.execute(res -> {
					if(res.succeeded()) {
											
						List<SensorLuz> sLuzList = new ArrayList<>();

						res.result().forEach(row -> {
							SensorLuz sl = new SensorLuz(row.getInteger("idSLuz"), 
									row.getDouble("valor"),
									row.getLong("tStamp"),
									row.getInteger("idPlaca"),
									row.getInteger("idGroup"),
									row.getInteger("id"));
							sLuzList.add(sl);
						});
						routingContext.response()
						.putHeader("content-type", "application/json")
                        .end(gson.toJson(sLuzList));
						
					}else {
						routingContext.response().setStatusCode(500).end("Database error: " + res.cause().getMessage());
					}
					connection.result().close();// Importante cerrar siempre la conexion
				});
			}else {
				routingContext.response().setStatusCode(500).end("Connection error: " + connection.cause().getMessage());
			}
		});
	}
	
	private void getAllSLuzWithId(RoutingContext routingContext) {
		
		mySqlClient.getConnection(connection -> {
			if(connection.succeeded()) {
				// Si se establece conexión, extraigo el id del Rele que me interesa para la consulta:
				Integer idSLuz = Integer.valueOf(routingContext.request().getParam("idSLuz"));
				
				connection.result()
				.preparedQuery("SELECT * FROM domoticadad.sLuz WHERE idSLuz = ? ")
				.execute(Tuple.of(idSLuz), res -> {
					if(res.succeeded()) {
						// Si se hace la consulta correctamente, creo una lista en la que almaceno las entradas del rele
						List<SensorLuz> sLuzList = new ArrayList<>();
						
						res.result().forEach(row -> {
							SensorLuz sl = new SensorLuz(row.getInteger("idSLuz"), 
									row.getDouble("valor"),
									row.getLong("tStamp"),
									row.getInteger("idPlaca"),
									row.getInteger("idGroup"),
									row.getInteger("id"));
							sLuzList.add(sl);
						});
						routingContext.response()
						.putHeader("content-type", "application/json")
                        .end(gson.toJson(sLuzList));
					}else {
						routingContext.response().setStatusCode(500).end("Database error: " + res.cause().getMessage());
					}
					connection.result().close();
				});
				
			}else {
				routingContext.response().setStatusCode(500).end("Connection error: " + connection.cause().getMessage());
			}
		});
	}
	
	private void getLastSLuzWithId(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			if(connection.succeeded()) {
				Integer idSLuz = Integer.valueOf(routingContext.request().getParam("idSLuz"));
				
				connection.result()
				.preparedQuery("SELECT * FROM domoticadad.sLuz WHERE idSLuz = ? ORDER BY tStamp DESC LIMIT 1")
				.execute(Tuple.of(idSLuz), res -> {
					if(res.succeeded()) {
						
						// res.result() da un Set<row>. Como en este caso solo va a tener una fila, extraemos el "next" 
						// del iterador, evitando bucles o cosas raras que funcionan en otras consultas pero enturbiarian esta
						Row row = res.result().iterator().next();
						
						SensorLuz sl = new SensorLuz(row.getInteger("idSLuz"), 
								row.getDouble("valor"),
								row.getLong("tStamp"),
								row.getInteger("idPlaca"),
								row.getInteger("idGroup"),
								row.getInteger("id"));
						
						routingContext.response()
						.putHeader("content-type", "application/json")
                        .end(gson.toJson(sl));
						
					}else {
						routingContext.response().setStatusCode(500).end("Database error: " + res.cause().getMessage());
					}
					connection.result().close();
				});
				
			}else {
				routingContext.response().setStatusCode(500).end("Connection error: " + connection.cause().getMessage());
			}
		});
	}
	
	private void getAllSLuzFromGroup(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			if(connection.succeeded()) {
				
				Integer idGroup = Integer.valueOf(routingContext.request().getParam("idGroup"));
				
				connection.result()
				.preparedQuery("SELECT r1.*"
						+ " FROM domoticadad.sLuz r1 "
						+ " INNER JOIN ("
						+ "    SELECT idSLuz, MAX(tStamp) AS maxTimestamp"
						+ "    FROM domoticadad.sLuz"
						+ "    WHERE idGroup = ?"
						+ "    GROUP BY idSLuz"
						+ ") r2 ON r1.idSLuz = r2.idSLuz AND r1.tStamp = r2.maxTimestamp"
						+ " WHERE r1.idGroup = ?")
				.execute(Tuple.of(idGroup, idGroup), res -> {
					if(res.succeeded()) {
						List<SensorLuz> sLuzList = new ArrayList<>();
						
						res.result().forEach(row -> {
							SensorLuz sl = new SensorLuz(row.getInteger("idSLuz"), 
									row.getDouble("valor"),
									row.getLong("tStamp"),
									row.getInteger("idPlaca"),
									row.getInteger("idGroup"),
									row.getInteger("id"));
							sLuzList.add(sl);
						});
						routingContext.response()
						.putHeader("content-type", "application/json")
                        .end(gson.toJson(sLuzList));
					}else {
						routingContext.response().setStatusCode(500).end("Database error: " + res.cause().getMessage());
					}
					connection.result().close();
				});
				
			}else {
				routingContext.response().setStatusCode(500).end("Connection error: " + connection.cause().getMessage());
			}
		});
	}
	
	private void addOneSLuz(RoutingContext routingContext) {
		
		
		// 1º - Extraemos los parámetros:
		JsonObject body = routingContext.body().asJsonObject();
		
		Integer idSLuz = body.getInteger("idSLuz");
		Double valor = body.getDouble("valor");
		Long tStamp = System.currentTimeMillis();
		Integer idPlaca = body.getInteger("idPlaca");
		Integer idGrupo = body.getInteger("idGroup");
		
		// 2º - Intentamos el POST
		mySqlClient.getConnection(connection ->{
			if(connection.succeeded()) {
				connection.result()
				.preparedQuery("INSERT INTO domoticadad.sLuz (idSLuz, valor, tStamp, idPlaca, idGroup) "
						+ "VALUES (?,?,?,?,?)").execute(Tuple.of(idSLuz, valor, tStamp, idPlaca, idGrupo),
						res ->{
							if(res.succeeded()) {
								routingContext.response().setStatusCode(201).end("SensorLuz añadido correctamente");
							}else {
								routingContext.response().setStatusCode(500).end("Database error: " + res.cause().getMessage());
							}
						connection.result().close();	
						});
			}else {
				routingContext.response().setStatusCode(500).end("Connection error: " + connection.cause().getMessage());
			}
		});
	}
	
	
	
	
	// sensores Temp
	
	private void getAllSTemp(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			if(connection.succeeded()) {
				connection.result()
				.preparedQuery("SELECT * FROM domoticadad.sTemp;")
				.execute(res -> {
					if(res.succeeded()) {
											
						List<SensorTemperatura> sTempList = new ArrayList<>();

						res.result().forEach(row -> {
							SensorTemperatura st = new SensorTemperatura(row.getInteger("idSTemp"), 
									row.getDouble("valor"),
									row.getLong("tStamp"),
									row.getInteger("idPlaca"),
									row.getInteger("idGroup"),
									row.getInteger("id"));
							sTempList.add(st);
						});
						routingContext.response()
						.putHeader("content-type", "application/json")
                        .end(gson.toJson(sTempList));
						
					}else {
						routingContext.response().setStatusCode(500).end("Database error: " + res.cause().getMessage());
					}
					connection.result().close();// Importante cerrar siempre la conexion
				});
			}else {
				routingContext.response().setStatusCode(500).end("Connection error: " + connection.cause().getMessage());
			}
		});
	}
	
	private void getAllSTempWithId(RoutingContext routingContext) {
		
		mySqlClient.getConnection(connection -> {
			if(connection.succeeded()) {
				// Si se establece conexión, extraigo el id del Rele que me interesa para la consulta:
				Integer idSTemp = Integer.valueOf(routingContext.request().getParam("idSTemp"));
				
				connection.result()
				.preparedQuery("SELECT * FROM domoticadad.sTemp WHERE idSTemp = ? ")
				.execute(Tuple.of(idSTemp), res -> {
					if(res.succeeded()) {
						// Si se hace la consulta correctamente, creo una lista en la que almaceno las entradas del rele
						List<SensorTemperatura> sTempList = new ArrayList<>();
						
						res.result().forEach(row -> {
							SensorTemperatura st = new SensorTemperatura(row.getInteger("idSTemp"), 
									row.getDouble("valor"),
									row.getLong("tStamp"),
									row.getInteger("idPlaca"),
									row.getInteger("idGroup"),
									row.getInteger("id"));
							sTempList.add(st);
						});
						routingContext.response()
						.putHeader("content-type", "application/json")
                        .end(gson.toJson(sTempList));
					}else {
						routingContext.response().setStatusCode(500).end("Database error: " + res.cause().getMessage());
					}
					connection.result().close();
				});
				
			}else {
				routingContext.response().setStatusCode(500).end("Connection error: " + connection.cause().getMessage());
			}
		});
	}
	
	private void getLastSTempWithId(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			if(connection.succeeded()) {
				Integer idSTemp = Integer.valueOf(routingContext.request().getParam("idSTemp"));
				
				connection.result()
				.preparedQuery("SELECT * FROM domoticadad.sTemp WHERE idSTemp = ? ORDER BY tStamp DESC LIMIT 1")
				.execute(Tuple.of(idSTemp), res -> {
					if(res.succeeded()) {
						
						// res.result() da un Set<row>. Como en este caso solo va a tener una fila, extraemos el "next" 
						// del iterador, evitando bucles o cosas raras que funcionan en otras consultas pero enturbiarian esta
						Row row = res.result().iterator().next();
						
						SensorTemperatura st = new SensorTemperatura(row.getInteger("idSTemp"), 
								row.getDouble("valor"),
								row.getLong("tStamp"),
								row.getInteger("idPlaca"),
								row.getInteger("idGroup"),
								row.getInteger("id"));
						
						routingContext.response()
						.putHeader("content-type", "application/json")
                        .end(gson.toJson(st));
						
					}else {
						routingContext.response().setStatusCode(500).end("Database error: " + res.cause().getMessage());
					}
					connection.result().close();
				});
				
			}else {
				routingContext.response().setStatusCode(500).end("Connection error: " + connection.cause().getMessage());
			}
		});
	}
	
	private void getAllSTempFromGroup(RoutingContext routingContext) {
		mySqlClient.getConnection(connection -> {
			if(connection.succeeded()) {
				
				Integer idGroup = Integer.valueOf(routingContext.request().getParam("idGroup"));
				
				connection.result()
				.preparedQuery("SELECT r1.*"
						+ " FROM domoticadad.sTemp r1 "
						+ " INNER JOIN ("
						+ "    SELECT idSTemp, MAX(tStamp) AS maxTimestamp"
						+ "    FROM domoticadad.sTemp"
						+ "    WHERE idGroup = ?"
						+ "    GROUP BY idSTemp"
						+ ") r2 ON r1.idSTemp = r2.idSTemp AND r1.tStamp = r2.maxTimestamp"
						+ " WHERE r1.idGroup = ?")
				.execute(Tuple.of(idGroup, idGroup), res -> {
					if(res.succeeded()) {
						List<SensorTemperatura> sTempList = new ArrayList<>();
						
						res.result().forEach(row -> {
							SensorTemperatura st = new SensorTemperatura(row.getInteger("idSTemp"), 
									row.getDouble("valor"),
									row.getLong("tStamp"),
									row.getInteger("idPlaca"),
									row.getInteger("idGroup"),
									row.getInteger("id"));
							sTempList.add(st);
						});
						routingContext.response()
						.putHeader("content-type", "application/json")
                        .end(gson.toJson(sTempList));
					}else {
						routingContext.response().setStatusCode(500).end("Database error: " + res.cause().getMessage());
					}
					connection.result().close();
				});
				
			}else {
				routingContext.response().setStatusCode(500).end("Connection error: " + connection.cause().getMessage());
			}
		});
	}
	
	private void addOneSTemp(RoutingContext routingContext) {
		
		
		// 1º - Extraemos los parámetros:
		JsonObject body = routingContext.body().asJsonObject();
		
		Integer idSTemp = body.getInteger("idSTemp");
		Double valor = body.getDouble("valor");
		Long tStamp = System.currentTimeMillis();
		Integer idPlaca = body.getInteger("idPlaca");
		Integer idGrupo = body.getInteger("idGroup");
		
		// 2º - Intentamos el POST
		mySqlClient.getConnection(connection ->{
			if(connection.succeeded()) {
				connection.result()
				.preparedQuery("INSERT INTO domoticadad.sTemp (idSTemp, valor, tStamp, idPlaca, idGroup) "
						+ "VALUES (?,?,?,?,?)").execute(Tuple.of(idSTemp, valor, tStamp, idPlaca, idGrupo),
						res ->{
							if(res.succeeded()) {
								routingContext.response().setStatusCode(201).end("SensorTemp añadido correctamente");
							}else {
								routingContext.response().setStatusCode(500).end("Database error: " + res.cause().getMessage());
							}
						connection.result().close();	
						});
			}else {
				routingContext.response().setStatusCode(500).end("Connection error: " + connection.cause().getMessage());
			}
		});
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
