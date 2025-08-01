package de.muenchen.oss.dbs.fk.domain.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class Form {
    private final Map<String, Object> inputs = new HashMap<>();
    private String lhmExtID;
    @JsonProperty(value = "authlevel", defaultValue = "level1")
    private String authLevel;
    private String accountSource;
    @JsonProperty("givenName")
    private String firstname;
    @JsonProperty("surname")
    private String lastname;
    @JsonProperty("handelndePersonVorname")
    private String actingPersonFirstname;
    @JsonProperty("handelndePersonNachname")
    private String actingPersonLastname;
    @JsonProperty("datenuebermittlerPseudonymId")
    private String pseudonymId;
    private String mail;
    // TODO translate
    private String ticketingZusatz;
    private String ticketingAnliegenart;
    @JsonProperty(defaultValue = "level1")
    private String ticketingVertrauensniveau;

    public enum AccountSource {
        ELSTER_NEZO
    }

    @JsonAnySetter
    public void addInput(final String key, final Object value) {
        this.inputs.put(key, value);
    }

    // TODO check
    @AssertTrue
    protected boolean isValid() {
        return this.getFirstname() != null || this.getLastname() != null || this.mail != null;
    }

    public String getFirstname() {
        if (accountSource.equals(AccountSource.ELSTER_NEZO.name()) && StringUtils.isNotBlank(this.actingPersonFirstname)
                && StringUtils.isNotBlank(this.actingPersonLastname)) {
            return this.actingPersonFirstname;
        }
        return this.firstname;
    }

    public String getLastname() {
        if (accountSource.equals(AccountSource.ELSTER_NEZO.name()) && StringUtils.isNotBlank(this.actingPersonFirstname)
                && StringUtils.isNotBlank(this.actingPersonLastname)) {
            return this.actingPersonLastname;
        }
        return this.lastname;
    }
}
