package rest;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import sensores_actuadores.ActuadorRele;
import sensores_actuadores.ActuadorReleListWrapper;
import sensores_actuadores.SensorLuz;
import sensores_actuadores.SensorLuzListWrapper;
import sensores_actuadores.SensorTemperatura;
import sensores_actuadores.SensorTemperaturaListWrapper;

public class RestServer extends AbstractVerticle {
	
	// Estos maps actúan de "base de datos"
	private Map<Integer, ActuadorRele> reles = new HashMap<Integer, ActuadorRele>();
	private Map<Integer, SensorLuz> sensoresLuz = new HashMap<Integer, SensorLuz>();
	private Map<Integer, SensorTemperatura> sensoresTemp = new HashMap<Integer, SensorTemperatura>();
	
	private Gson gson;
	
	public void start(Promise<Void> startFuture) {
		// Datos de ejemplo
		 createSomeData(10);
		
		// Instanciamos Gson
		gson = new GsonBuilder().setDateFormat("dd-MM-yyyy").create();
		
		// Definimos el objeto router:
		Router router = Router.router(vertx);
		
		// Tratamos los posibles resultados del despliegue del server:
		vertx.createHttpServer().requestHandler(router::handle).listen(8084, result -> {
			if(result.succeeded())startFuture.complete();
			else startFuture.fail(result.cause());
		});
		
		// Aquí definimos las URI para cada método de la API
		// Estructura: router + ."tipo de método (get, post, etc)" + .handler( "función que sobre la base de datos" );
		
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
	
	// Funciones para la API
		
	// Relés
	
	// Devuelve TODAS las entradas de reles
	private void getAllReles(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
		.end(gson.toJson(new ActuadorReleListWrapper(reles.values().stream().collect(Collectors.toList()))));
	}
	
	// Devuelve todas las entradas de un rele concreto
	private void getAllReleWithId(RoutingContext routingContext) {
		// Ojo, este id lo recogemos como String, pero a la hora de compararlo, lo transformaremos en Integer
		final String idRele = routingContext.request().getParam("idRele");
				
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
		.end(gson.toJson(new ActuadorReleListWrapper(reles.values().stream().filter(x -> x!=null).filter( elem -> 
			elem.getIdRele().equals(Integer.valueOf(idRele))
		).collect(Collectors.toList()))));
	}
	// Devuelve la ultima entrada de un rele concreto
	private void getLastReleWithId(RoutingContext routingContext) {
		
		try {
			// Extraemos el id de la url
			final int idRele = Integer.parseInt(routingContext.request().getParam("idRele"));
			
			
			if(reles.values().stream().filter(x -> x!=null).filter(x -> x.getIdRele()==idRele).count() !=0) {
				// Ordenamos por timestamp y cogemos el que tenga mayor de ellos. Es decir, el último añadido
				ActuadorRele ar = reles.values().stream().filter(x -> x!=null).filter(x -> x.getIdRele()==idRele)
						.sorted(Comparator.comparing(ActuadorRele::getTimestamp).reversed())
						.findFirst().get();
				
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(200).end(ar!=null?gson.toJson(ar) : "");
				
			}else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(204).end();
			}
			
			
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
			.end();
		}
	}
	
	// Devuelve la ultima entrada de todos los reles de un mismo grupo
	private void getAllRelesFromGroup(RoutingContext routingContext) {
		try {
			// Extraemos la id del grupo:
			final int idGrupo = Integer.parseInt(routingContext.request().getParam("idGroup"));
			
			// Recogemos todos los reles de dicho grupo
			List<ActuadorRele> relesGrupo = reles.values().stream().filter(x -> x!=null).filter(x -> x.getIdGroup() == idGrupo).toList(); 
			
			// Si hay al menos una entrada:
			if(!relesGrupo.isEmpty()) {
				// Creo un map que almacenará una entrada de cada relé perteneciente al grupo en el que estamos
				Map<Integer, ActuadorRele> estado = new HashMap<>();
				
				// Por cada entrada asociada al grupo:
				for(ActuadorRele ar : relesGrupo) {
					// Si ya tenemos una entrada del relé que estamos estudiando
					
					if(estado.keySet().contains(ar.getIdRele())) {
						// Comprueba si la entrada actual es más reciente que la entrada anterior
						if(estado.get(ar.getIdRele()).getTimestamp()<ar.getTimestamp()) {
							// Si la actual es más reciente, actualiza el valor
							estado.put(ar.getIdRele(), ar);
						}
					// Si no teníamos entrada para ese relé, añadimos la entrada actual
					}else {
						estado.put(ar.getIdRele(), ar);
					}
				}
				
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(200).end(
						gson.toJson(new ActuadorReleListWrapper(estado.values().stream().collect(Collectors.toList())))
						);
			}else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(204).end();
			}
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
			.setStatusCode(204).end();
		}
	}
	
	private void addOneRele(RoutingContext routingContext) {
		try {
			
			final ActuadorRele ar = gson.fromJson(routingContext.getBodyAsString(), ActuadorRele.class);
			
			// Creamos un "id aleatorio" que va en función del timestamp actual
			// Este id es para las pruebas, puesto que en la base de datos se añadirá correctamente
			Integer id = (int) System.currentTimeMillis();
			
			reles.put(id, ar);
			
			routingContext.response().setStatusCode(201).putHeader("content-type", "application/java charset=utf-8")
			.end(gson.toJson(ar));
		} catch (Exception e) {
			System.out.println(e);
		}
		
	}

	
	
	
	// sensoresLuz
	
	// Devuelve TODAS las entradas de sensoresLuz
	private void getAllSLuz(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
		.end(gson.toJson(new SensorLuzListWrapper(sensoresLuz.values().stream().collect(Collectors.toList()))));
	}
	// Devuelve todas las entradas de un sensorLuz concreto
	private void getAllSLuzWithId(RoutingContext routingContext) {
		// Ojo, este id lo recogemos como String, pero a la hora de compararlo, lo transformaremos en Integer
		final String idSLuz = routingContext.request().getParam("idSLuz");
				
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
		.end(gson.toJson(new SensorLuzListWrapper(sensoresLuz.values().stream().filter(x -> x!=null).filter( elem -> 
			elem.getIdFotoRes().equals(Integer.valueOf(idSLuz))
		).collect(Collectors.toList()))));
	}
	// Devuelve la ultima entrada de un sensorLuz concreto
	private void getLastSLuzWithId(RoutingContext routingContext) {
		
		try {
			// Extraemos el id de la url
			final int idSLuz = Integer.parseInt(routingContext.request().getParam("idSLuz"));
			
			
			if(sensoresLuz.values().stream().filter(x -> x!=null).filter(x -> x.getIdFotoRes()==idSLuz).count() !=0) {
				// Ordenamos por timestamp y cogemos el que tenga mayor de ellos. Es decir, el último añadido
				SensorLuz ar = sensoresLuz.values().stream().filter(x -> x!=null).filter(x -> x.getIdFotoRes()==idSLuz)
						.sorted(Comparator.comparing(SensorLuz::getTimestamp).reversed())
						.findFirst().get();
				
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(200).end(ar!=null?gson.toJson(ar) : "");
				
			}else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(204).end();
			}
			
			
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
			.end();
		}
	}
	
	// Devuelve la ultima entrada de todos los sensoresLuz de un mismo grupo
	private void getAllSLuzFromGroup(RoutingContext routingContext) {
		try {
			// Extraemos la id del grupo:
			final int idGrupo = Integer.parseInt(routingContext.request().getParam("idGroup"));
			
			// Recogemos todos los sensoresLuz de dicho grupo
			List<SensorLuz> sLuzGrupo = sensoresLuz.values().stream().filter(x -> x!=null)
					.filter(x -> x.getIdGroup() == idGrupo).toList(); 
			
			// Si hay al menos una entrada:
			if(!sLuzGrupo.isEmpty()) {
				// Creo un map que almacenará una entrada de cada relé perteneciente al grupo en el que estamos
				Map<Integer, SensorLuz> estado = new HashMap<>();
				
				// Por cada entrada asociada al grupo:
				for(SensorLuz sl : sLuzGrupo) {
					// Si ya tenemos una entrada del relé que estamos estudiando
					
					if(estado.keySet().contains(sl.getIdFotoRes())) {
						// Comprueba si la entrada actual es más reciente que la entrada anterior
						if(estado.get(sl.getIdFotoRes()).getTimestamp()<sl.getTimestamp()) {
							// Si la actual es más reciente, actualiza el valor
							estado.put(sl.getIdFotoRes(), sl);
						}
					// Si no teníamos entrada para ese relé, añadimos la entrada actual
					}else {
						estado.put(sl.getIdFotoRes(), sl);
					}
				}
				
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(200).end(
						gson.toJson(new SensorLuzListWrapper(estado.values().stream().collect(Collectors.toList())))
						);
			}else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(204).end();
			}
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
			.setStatusCode(204).end();
		}
	}
	
	private void addOneSLuz(RoutingContext routingContext) {
		try {
			
			final SensorLuz sl = gson.fromJson(routingContext.getBodyAsString(), SensorLuz.class);
			
			// Creamos un "id aleatorio" que va en función del timestamp actual
			// Este id es para las pruebas, puesto que en la base de datos se añadirá correctamente
			Integer id = (int) System.currentTimeMillis();
			
			sensoresLuz.put(id, sl);
			
			routingContext.response().setStatusCode(201).putHeader("content-type", "application/java charset=utf-8")
			.end(gson.toJson(sl));
		} catch (Exception e) {
			System.out.println(e);
		}
		
	}
	
	
	
	// sensoresTemp
	
	// Devuelve TODAS las entradas de sensoresTemp
	private void getAllSTemp(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
		.end(gson.toJson(new SensorTemperaturaListWrapper(sensoresTemp.values().stream().collect(Collectors.toList()))));
	}
	
	// Devuelve todas las entradas de un sensorTemp concreto
	private void getAllSTempWithId(RoutingContext routingContext) {
		// Ojo, este id lo recogemos como String, pero a la hora de compararlo, lo transformaremos en Integer
		final String idSTemp = routingContext.request().getParam("idSTemp");
				
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
		.end(gson.toJson(new SensorTemperaturaListWrapper(sensoresTemp.values().stream().filter(x -> x!=null).filter( elem -> 
			elem.getIdTemp().equals(Integer.valueOf(idSTemp))
		).collect(Collectors.toList()))));
	}
	// Devuelve la ultima entrada de un sensorTemp concreto
	private void getLastSTempWithId(RoutingContext routingContext) {
		
		try {
			// Extraemos el id de la url
			final int idSTemp = Integer.parseInt(routingContext.request().getParam("idSTemp"));
			
			
			if(sensoresTemp.values().stream().filter(x -> x!=null).filter(x -> x.getIdTemp()==idSTemp).count() !=0) {
				// Ordenamos por timestamp y cogemos el que tenga mayor de ellos. Es decir, el último añadido
				SensorTemperatura st = sensoresTemp.values().stream().filter(x -> x!=null).filter(x -> x.getIdTemp()==idSTemp)
						.sorted(Comparator.comparing(SensorTemperatura::getTimestamp).reversed())
						.findFirst().get();
				
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(200).end(st!=null?gson.toJson(st) : "");
				
			}else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(204).end();
			}
			
			
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
			.end();
		}
	}
	
	// Devuelve la ultima entrada de todos los sensoresTemp de un mismo grupo
	private void getAllSTempFromGroup(RoutingContext routingContext) {
		try {
			// Extraemos la id del grupo:
			final int idGrupo = Integer.parseInt(routingContext.request().getParam("idGroup"));
			
			// Recogemos todos los sensorTemp de dicho grupo
			List<SensorTemperatura> sTempGrupo = sensoresTemp.values().stream().filter(x -> x!=null).filter(x -> x.getIdGroup() == idGrupo).toList(); 
			
			// Si hay al menos una entrada:
			if(!sTempGrupo.isEmpty()) {
				// Creo un map que almacenará una entrada de cada relé perteneciente al grupo en el que estamos
				Map<Integer, SensorTemperatura> estado = new HashMap<>();
				
				// Por cada entrada asociada al grupo:
				for(SensorTemperatura st : sTempGrupo) {
					// Si ya tenemos una entrada del relé que estamos estudiando
					
					if(estado.keySet().contains(st.getIdTemp())) {
						// Comprueba si la entrada actual es más reciente que la entrada anterior
						if(estado.get(st.getIdTemp()).getTimestamp()<st.getTimestamp()) {
							// Si la actual es más reciente, actualiza el valor
							estado.put(st.getIdTemp(), st);
						}
					// Si no teníamos entrada para ese relé, añadimos la entrada actual
					}else {
						estado.put(st.getIdTemp(), st);
					}
				}
				
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(200).end(
						gson.toJson(new SensorTemperaturaListWrapper(estado.values().stream().collect(Collectors.toList())))
						);
			}else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(204).end();
			}
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
			.setStatusCode(204).end();
		}
	}
	
	private void addOneSTemp(RoutingContext routingContext) {
		try {
			
			final SensorTemperatura st = gson.fromJson(routingContext.getBodyAsString(), SensorTemperatura.class);
			
			// Creamos un "id aleatorio" que va en función del timestamp actual
			// Este id es para las pruebas, puesto que en la base de datos se añadirá correctamente
			Integer id = (int) System.currentTimeMillis();
			
			sensoresTemp.put(id, st);
			
			routingContext.response().setStatusCode(201).putHeader("content-type", "application/java charset=utf-8")
			.end(gson.toJson(st));
		} catch (Exception e) {
			System.out.println(e);
		}
		
	}
	
	
	
		
	// Función auxiliar para crear datos de prueba
	private void createSomeData(int number) {
		Random rnd = new Random();
		IntStream.range(0, number).forEach(elem -> {
			int id = Math.abs(rnd.nextInt());
			reles.put(id, new ActuadorRele(id, id%2, id%25, id%15, id+1));
			sensoresLuz.put(id, new SensorLuz(id, Double.valueOf(id*2), id%25, id%15, id+1));
			sensoresTemp.put(id, new SensorTemperatura(id, Double.valueOf(id*2), id%25, id%15, id+1));
		});
	}
	
	
	
	
}
