package entregable1;

import java.util.Objects;

public class Rele {
	private Integer idRele;
	private Boolean estado;
	private Long timestamp;
	private Integer idPlaca;
	private Integer idGroup;
	private Integer idDB;
	// Esta clase cuenta con el atributo tipo ya que el entregable1 se ha realizado DESPUÉS de finalizar todo lo demás
	// Hasta el "Proyecto Final" no se volverá a considerar dicho atributo ni ninguna lógica de negocio asociada a este
	private String tipo;
	
	public Rele(Integer idRele, Boolean estado, Integer idPlaca, Integer idGroup,
			Integer idDB, String tipo) {
		super();
		this.idRele = idRele;
		
		this.estado = estado;

		this.timestamp = System.currentTimeMillis();
		this.idPlaca = idPlaca;
		this.idGroup = idGroup;
		this.idDB = idDB;
		this.tipo = tipo;
	}

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

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	@Override
	public int hashCode() {
		return Objects.hash(estado, idDB, idGroup, idPlaca, idRele, timestamp, tipo);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rele other = (Rele) obj;
		return Objects.equals(estado, other.estado) && Objects.equals(idDB, other.idDB)
				&& Objects.equals(idGroup, other.idGroup) && Objects.equals(idPlaca, other.idPlaca)
				&& Objects.equals(idRele, other.idRele) && Objects.equals(timestamp, other.timestamp)
				&& Objects.equals(tipo, other.tipo);
	}

	@Override
	public String toString() {
		return "Rele [idRele=" + idRele + ", estado=" + estado + ", timestamp=" + timestamp + ", idPlaca=" + idPlaca
				+ ", idGroup=" + idGroup + ", idDB=" + idDB + ", tipo=" + tipo + "]";
	}
	
	
}
