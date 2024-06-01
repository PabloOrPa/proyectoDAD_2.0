package sensores_actuadores;

import java.util.Objects;

public class ActuadorRele {
	
	private Integer idRele;
	private Boolean estado;
	private Long timestamp;
	private Integer idPlaca;
	private Integer idGroup;
	private Integer idDB;
	
	public ActuadorRele(Integer idRele, Integer estado, Integer idPlaca, Integer idGroup,
			Integer idDB) {
		super();
		this.idRele = idRele;
		
		if(estado==0)this.estado=false;
		else if(estado==1)this.estado=true;
		else this.estado = null;
		
		this.timestamp = System.currentTimeMillis();
		this.idPlaca = idPlaca;
		this.idGroup = idGroup;
		this.idDB = idDB;
	}/*
	public ActuadorRele(String idRele, String estado, String idPlaca, String idGroup,
			String idDB) {
		super();
		this.idRele = Integer.valueOf(idRele);
		
		if(estado=="false")this.estado=false;
		else if(estado=="true")this.estado=true;
		else this.estado = null;
		
		this.timestamp = System.currentTimeMillis();
		this.idPlaca = Integer.valueOf(idPlaca);
		this.idGroup = Integer.valueOf(idGroup);
		this.idDB = Integer.valueOf(idDB);
	}*/

	public Integer getIdRele() {
		return idRele;
	}

	public void setIdRele(Integer idRele) {
		this.idRele = idRele;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getIdPlaca() {
		return idPlaca;
	}

	public void setIdPlaca(Integer idPlaca) {
		this.idPlaca = idPlaca;
	}

	public Integer getIdGroup() {
		return idGroup;
	}

	public void setIdGroup(Integer idGroup) {
		this.idGroup = idGroup;
	}

	public Integer getIdDB() {
		return idDB;
	}

	public void setIdDB(Integer idDB) {
		this.idDB = idDB;
	}

	@Override
	public int hashCode() {
		return Objects.hash(estado, idDB, idGroup, idPlaca, idRele, timestamp);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActuadorRele other = (ActuadorRele) obj;
		return Objects.equals(estado, other.estado) && Objects.equals(idDB, other.idDB)
				&& Objects.equals(idGroup, other.idGroup) && Objects.equals(idPlaca, other.idPlaca)
				&& Objects.equals(idRele, other.idRele) && Objects.equals(timestamp, other.timestamp);
	}

	@Override
	public String toString() {
		return "ActuadorRele [idRele=" + idRele + ", estado=" + estado + ", timestamp=" + timestamp + ", idPlaca="
				+ idPlaca + ", idGroup=" + idGroup + ", idDB=" + idDB + "]";
	}
	
	


	

	
}

