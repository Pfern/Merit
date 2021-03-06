package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Revocation extends Model {

	public Revocation(Long assertionUID, String reason) {
		// TODO Auto-generated constructor stub
		this.uid = assertionUID;
		this.reason = reason;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3186644815463858012L;
	@Id
	public Long uid;
	@Required
	public String reason;

	public static Model.Finder<Long, Revocation> find = new Model.Finder<Long, Revocation>(
			Long.class, Revocation.class);
}
