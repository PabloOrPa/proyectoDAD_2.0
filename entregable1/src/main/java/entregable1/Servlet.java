package entregable1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class Servlet extends HttpServlet{
	
	private static final long serialVersionUID = 1L;// Para evitar la advertencia (aunque ya esté en desuso)
	
	private List<Rele> reles;
	
	public void init() throws ServletException{
		reles = new ArrayList<Rele>();
		creaDatosEjemplo(25);
		super.init();
	}
	
	
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// Devuelve todos los valores de un Rele concreto de una placa concreta
		if(req.getParameter("idRele")==null || req.getParameter("idPlaca")==null) {
			PrintWriter out = resp.getWriter();
			out.println("<html>");
			out.println("<body>");
			out.println("<h1>" + "Bienvenido al Servlet del entregable 1"+"</h1>"
					+ "<br>"
					+ "<h2>"+"Por favor, introduce en la url idRele e idPlaca validos" + "</h2>"
					+ "<h3>"+"Estos son los disponibles: " + "</h3>"
					);
			for(int i=0; i<this.reles.size();i++) {
				out.println(
						"<br><h3>"+
						this.reles.get(i)
						+"</h3>"
						);
			}
			out.println("</body>");
			out.println("</html>");
			
		}else {
		
			Integer idRele = Integer.valueOf(req.getParameter("idRele"));
			Integer idPlaca = Integer.valueOf(req.getParameter("idPlaca"));
			
			Gson gson = new Gson();
			
			// Declaramos un array de Json donde almacenaremos los reles
			List<Rele> lista = new ArrayList<>();
			
			for(Rele r : this.reles) {
				if(r.getIdRele().equals(idRele) && r.getIdPlaca().equals(idPlaca)) {
					lista.add(r);
				}
			}
			// Si no está vacío, damos una buena respuesta
			if(!lista.isEmpty()) {
				// Creamos un objeto Json con el array, para no devolver un continente de json, sino un json de json
				JsonObject respuesta = new JsonObject();
				for(Rele rele: lista) {
					respuesta.add("rele", gson.toJsonTree(rele));
				}
				// Damos respuesta a la petición
				resp.getWriter().println(respuesta);
				resp.setStatus(201);
			}else {
				resp.getWriter().println("Rele no encontrado");
				resp.setStatus(300);
			}
		}
		
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		BufferedReader reader = req.getReader();
		
		Gson gson = new Gson();
		try {
			Rele rele = gson.fromJson(reader, Rele.class);
			if(rele.getIdPlaca()==null||rele.getIdRele()==null||rele.getIdGroup()==null||rele.getIdDB()==null||rele.getTipo()==null) {
				resp.getWriter().print("Formulario incompleto, faltan datos");
				resp.setStatus(300);
			}else {
				reles.add(rele);
				resp.getWriter().println(gson.toJson(rele));
				resp.setStatus(201);
			}
				
		}catch(Exception e) {
			resp.getWriter().println("Los datos facilitados no son válidos.\n"
					+ "Asegurese de que envía: \n"
					+ "Integer,Boolean,Integer,Integer,Integer,String");
			resp.setStatus(500);
		}
		
		
	}
	
	
	
	
	private void creaDatosEjemplo(Integer cantidad) {
		
		Random rand = new Random();
		for(int i=0; i<cantidad; i++) {
			this.reles.add(new Rele(
					Integer.valueOf(rand.nextInt(20)),
					rand.nextInt(2)==0?false:true,
					Integer.valueOf(rand.nextInt(10)),
					Integer.valueOf(rand.nextInt(5)),
					Integer.valueOf(rand.nextInt(10000)),
					rand.nextInt(2)==0?"Bombilla":"Ventilador"
					));
		}
	}
	
}
