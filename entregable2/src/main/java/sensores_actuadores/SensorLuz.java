package sensores_actuadores;

import java.util.Objects;

public class SensorLuz {
	
	private Integer idFotoRes;
	private Double valor;
	private Long timestamp;
	private Integer idPlaca;
	private Integer idGroup;
	private Integer idDB;
	
	public SensorLuz(Integer idFotoRes, Double valor, Integer idPlaca, Integer idGroup, Integer idDB) {
		super();
		this.idFotoRes = idFotoRes;
		this.valor = valor;
		this.timestamp = System.currentTimeMillis();
		this.idPlaca = idPlaca;
		this.idGroup = idGroup;
		this.idDB = idDB;
	}

	public Integer getIdFotoRes() {
		return idFotoRes;
	}

	public void setIdFotoRes(Integer idFotoRes) {
		this.idFotoRes = idFotoRes;
	}

	public Double getValor() {
		return valor;
	}

	public void setValor(Double valor) {
		this.valor = valor;
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
		return Objects.hash(idDB, idFotoRes, idGroup, idPlaca, timestamp, valor);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SensorLuz other = (SensorLuz) obj;
		return Objects.equals(idDB, other.idDB) && Objects.equals(idFotoRes, other.idFotoRes)
				&& Objects.equals(idGroup, other.idGroup) && Objects.equals(idPlaca, other.idPlaca)
				&& Objects.equals(timestamp, other.timestamp) && Objects.equals(valor, other.valor);
	}

	@Override
	public String toString() {
		return "SensorLuz [idFotoRes=" + idFotoRes + ", valor=" + valor + ", timestamp=" + timestamp + ", idPlaca="
				+ idPlaca + ", idGroup=" + idGroup + ", idDB=" + idDB + "]";
	}
	
	
	
	
	
	
	
	
}

