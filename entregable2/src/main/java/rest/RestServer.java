package rest;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import sensores_actuadores.SensorTemperatura;

public class RestServer extends AbstractVerticle {
	
	// Estos maps actúan de "base de datos"
	private Map<Integer, ActuadorRele> reles = new HashMap<Integer, ActuadorRele>();
	private Map<Integer, SensorLuz> sensoresLuz = new HashMap<Integer, SensorLuz>();
	private Map<Integer, SensorTemperatura> sensoresTemp = new HashMap<Integer, SensorTemperatura>();
	
	private Gson gson;
	
	public void start(Promise<Void> startFuture) {
		// Datos de ejemplo
		// createSomeData(25);
		
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
		router.get("/api/reles/all/:idRele").handler(this::getAllReleWithId);		// Entradas de un relé concreto (filtra por Id)
		router.get("/api/reles/last/:idRele").handler(this::getLastReleWithId);
		router.get("/api/reles/estado/:idGroup").handler(this::getAllRelesFromGroup);
		router.post("/api/reles").handler(this::addOne);
		
		// API sensoresLuz:
		router.route().handler(BodyHandler.create());
		router.get().handler(null);
		router.get().handler(null);
		router.get().handler(null);
		router.post().handler(null);
		
		// API sensoresTemp:
		router.route().handler(BodyHandler.create());
		router.get().handler(null);
		router.get().handler(null);
		router.get().handler(null);
		router.post().handler(null);
		
		
		
		
		
		
		
	}
	
	// Funciones para la API
		
	// Relés
	
	private void getAllReleWithId(RoutingContext routingContext) {
		// Ojo, este id lo recogemos como String, pero a la hora de compararlo, lo transformaremos en Integer
		final String idRele = routingContext.queryParams().contains("idRele") ? routingContext.queryParam("idRele").get(0) : null;
		
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
		.end(gson.toJson(new ActuadorReleListWrapper(reles.values().stream().filter( elem -> 
			elem.getIdRele().equals(Integer.valueOf(idRele))
		).collect(Collectors.toList()))));
	}
	
	private void getLastReleWithId(RoutingContext routingContext) {
		
		// Es necesaria la comprobación con la ultima actualización de la ruta??
		//final String idRele = routingContext.queryParams().contains("idRele") ? routingContext.queryParam("idRele").get(0):null;
		
		// Probemos con esto:
		
		try {
			// Extraemos el id de la url
			final int idRele = Integer.parseInt(routingContext.request().getParam("idRele"));
			
			
			if(reles.values().stream().filter(x -> x.getIdRele()==idRele).count() !=0) {
				// Ordenamos por timestamp y cogemos el que tenga mayor de ellos. Es decir, el último añadido
				ActuadorRele ar = reles.values().stream().filter(x -> x.getIdRele()==idRele)
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
	

	private void getAllRelesFromGroup(RoutingContext routingContext) {
		try {
			// Extraemos la id del grupo:
			final int idGrupo = Integer.parseInt(routingContext.request().getParam("idGroup"));
			// Recogemos todos los reles de dicho grupo
			List<ActuadorRele> relesGrupo = reles.values().stream().filter(x -> x.getIdGroup() == idGrupo).toList(); 
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
			}else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(204).end();
			}
		} catch (Exception e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
			.setStatusCode(204).end();
		}
	}
	
	private void addOne(RoutingContext routingContext) {
		final ActuadorRele ar = gson.fromJson(routingContext.getBodyAsString(), ActuadorRele.class);
		
		// Creamos un "id aleatorio" que va en función del timestamp actual
		// Este id es para las pruebas, puesto que en la base de datos se añadirá correctamente
		Integer id = (int) System.currentTimeMillis();
		
		reles.put(id, ar);
		
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/java charset=utf-8")
		.end(gson.toJson(ar));
	}
	
	
	
	
	
	
	
}
