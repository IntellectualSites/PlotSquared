package com.plotsquared.core.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "player_meta")
@NamedQueries({
        @NamedQuery(name = "PlayerMeta.findByUuid", query = "SELECT m FROM PlayerMetaEntity m WHERE m.uuid = :uuid"),
        @NamedQuery(name = "PlayerMeta.deleteByUuidAndKey", query = "DELETE FROM PlayerMetaEntity m WHERE m.uuid = :uuid AND m.key = :key")
})
public class PlayerMetaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meta_id")
    private Long id;
    @Column(length = 40, nullable = false)
    private String uuid;
    @Column(name = "key", length = 32, nullable = false)
    private String key;
    @Lob
    @Column(name = "value", nullable = false)
    private byte[] value;

    public PlayerMetaEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public byte[] getValue() { return value; }
    public void setValue(byte[] value) { this.value = value; }
}
