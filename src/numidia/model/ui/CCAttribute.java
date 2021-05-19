package numidia.model.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author bnazare
 */
public class CCAttribute {
    
    private static final Map<String,String> attrPrettyNames = new HashMap();
    
    static {
        attrPrettyNames.put("name", "Nome Completo");
        attrPrettyNames.put("givenName", "Nome[s]");
        attrPrettyNames.put("surName", "Apelido[s]");
        attrPrettyNames.put("nic", "N.º Identificação Civil");
        attrPrettyNames.put("expiryDate", "Data de Validade");
        
        attrPrettyNames.put("sex", "Sexo");
        attrPrettyNames.put("height", "Altura");
        attrPrettyNames.put("nationality", "Nacionalidade");
        attrPrettyNames.put("dateOfBirth", "Data de Nascimento");
        attrPrettyNames.put("givenNameFather", "Nome[s] do Pai");
        attrPrettyNames.put("surnameFather", "Apelido[s] do Pai");
        attrPrettyNames.put("givenNameMother", "Nome[s] da Mãe");
        attrPrettyNames.put("surnameMother", "Apelido[s] da Mãe");
        attrPrettyNames.put("notes", "Indicações Eventuais");
        
        attrPrettyNames.put("DocumentNo", "Nº de Documento");
        attrPrettyNames.put("TaxNo", "N.º Identificação Fiscal");
        attrPrettyNames.put("SocialSecurityNo", "N.º Segurança Social");
        attrPrettyNames.put("HealthNo", "N.º Utente de Saúde");
        attrPrettyNames.put("mrz1", "MRZ1");
        attrPrettyNames.put("mrz2", "MRZ1");
        attrPrettyNames.put("mrz3", "MRZ1");
        
        attrPrettyNames.put("cardVersion", "Versão do Cartão");
        attrPrettyNames.put("cardNumberPAN", "PAN");
        attrPrettyNames.put("issuingDate", "Data de emissão");
        attrPrettyNames.put("issuingEntity", "Entidade Emissora");
        attrPrettyNames.put("documentType", "Tipo de documento");
        attrPrettyNames.put("localOfRequest", "Local de Pedido");
        attrPrettyNames.put("version", "Versão");
        
        attrPrettyNames.put("district", "Distrito Nacional");
        attrPrettyNames.put("municipality", "Concelho");
        attrPrettyNames.put("civilParish", "Freguesia");
        attrPrettyNames.put("abrStreetType", "Abr. Tipo de Via");
        attrPrettyNames.put("streetType", "Tipo de Via");
        attrPrettyNames.put("streetName", "Designação da Via");
        attrPrettyNames.put("abrBuildingType", "Abr. Tipo de Edifício");
        attrPrettyNames.put("buildingType", "Tipo de Edifício");
        attrPrettyNames.put("doorNo", "N.º de Porta");
        attrPrettyNames.put("floor", "Andar");
        attrPrettyNames.put("side", "Lado");
        attrPrettyNames.put("place", "Lugar");
        attrPrettyNames.put("locality", "Localidade");
        attrPrettyNames.put("zip4", "CP4");
        attrPrettyNames.put("zip3", "CP3");
        attrPrettyNames.put("postalLocality", "Localidade Postal");
        
        attrPrettyNames.put("photo", "Fotografia");
        
        attrPrettyNames.put("userNotes", "Notas Pessoais");
    }

    private String name;
    private boolean optional;
    
    public CCAttribute(String name) {
        this.name = name;
        this.optional = false;
    }
    
    public CCAttribute(String name, boolean optional) {
        this.name = name;
        this.optional = optional;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CCAttribute other = (CCAttribute) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }
    
    public String getPrettyName() {
        return attrPrettyNames.get(name);
    }
}
