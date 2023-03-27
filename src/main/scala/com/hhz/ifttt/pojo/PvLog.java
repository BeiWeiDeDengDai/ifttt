/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.hhz.ifttt.pojo;

import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.SchemaStore;
import org.apache.avro.specific.SpecificData;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class PvLog extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -2742990992640439945L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"PvLog\",\"namespace\":\"com.hhz.ifttt.pojo\",\"fields\":[{\"name\":\"ip\",\"type\":\"string\"},{\"name\":\"uid\",\"type\":\"string\"},{\"name\":\"oid\",\"type\":\"string\"},{\"name\":\"did\",\"type\":\"string\"},{\"name\":\"tripper_id\",\"type\":\"string\"},{\"name\":\"is_login\",\"type\":\"string\"},{\"name\":\"from\",\"type\":\"string\"},{\"name\":\"type\",\"type\":\"string\"},{\"name\":\"status\",\"type\":\"string\"},{\"name\":\"phonesystem\",\"type\":\"string\"},{\"name\":\"phone\",\"type\":\"string\"},{\"name\":\"phone_system_version\",\"type\":\"string\"},{\"name\":\"app_version\",\"type\":\"string\"},{\"name\":\"time\",\"type\":\"string\"},{\"name\":\"all\",\"type\":\"string\"},{\"name\":\"day\",\"type\":\"int\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<PvLog> ENCODER =
      new BinaryMessageEncoder<PvLog>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<PvLog> DECODER =
      new BinaryMessageDecoder<PvLog>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   */
  public static BinaryMessageDecoder<PvLog> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   */
  public static BinaryMessageDecoder<PvLog> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<PvLog>(MODEL$, SCHEMA$, resolver);
  }

  /** Serializes this PvLog to a ByteBuffer. */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /** Deserializes a PvLog from a ByteBuffer. */
  public static PvLog fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  @Deprecated public CharSequence ip;
  @Deprecated public CharSequence uid;
  @Deprecated public CharSequence oid;
  @Deprecated public CharSequence did;
  @Deprecated public CharSequence tripper_id;
  @Deprecated public CharSequence is_login;
  @Deprecated public CharSequence from;
  @Deprecated public CharSequence type;
  @Deprecated public CharSequence status;
  @Deprecated public CharSequence phonesystem;
  @Deprecated public CharSequence phone;
  @Deprecated public CharSequence phone_system_version;
  @Deprecated public CharSequence app_version;
  @Deprecated public CharSequence time;
  @Deprecated public CharSequence all;
  @Deprecated public int day;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public PvLog() {}

  /**
   * All-args constructor.
   * @param ip The new value for ip
   * @param uid The new value for uid
   * @param oid The new value for oid
   * @param did The new value for did
   * @param tripper_id The new value for tripper_id
   * @param is_login The new value for is_login
   * @param from The new value for from
   * @param type The new value for type
   * @param status The new value for status
   * @param phonesystem The new value for phonesystem
   * @param phone The new value for phone
   * @param phone_system_version The new value for phone_system_version
   * @param app_version The new value for app_version
   * @param time The new value for time
   * @param all The new value for all
   * @param day The new value for day
   */
  public PvLog(CharSequence ip, CharSequence uid, CharSequence oid, CharSequence did, CharSequence tripper_id, CharSequence is_login, CharSequence from, CharSequence type, CharSequence status, CharSequence phonesystem, CharSequence phone, CharSequence phone_system_version, CharSequence app_version, CharSequence time, CharSequence all, Integer day) {
    this.ip = ip;
    this.uid = uid;
    this.oid = oid;
    this.did = did;
    this.tripper_id = tripper_id;
    this.is_login = is_login;
    this.from = from;
    this.type = type;
    this.status = status;
    this.phonesystem = phonesystem;
    this.phone = phone;
    this.phone_system_version = phone_system_version;
    this.app_version = app_version;
    this.time = time;
    this.all = all;
    this.day = day;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public Object get(int field$) {
    switch (field$) {
    case 0: return ip;
    case 1: return uid;
    case 2: return oid;
    case 3: return did;
    case 4: return tripper_id;
    case 5: return is_login;
    case 6: return from;
    case 7: return type;
    case 8: return status;
    case 9: return phonesystem;
    case 10: return phone;
    case 11: return phone_system_version;
    case 12: return app_version;
    case 13: return time;
    case 14: return all;
    case 15: return day;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, Object value$) {
    switch (field$) {
    case 0: ip = (CharSequence)value$; break;
    case 1: uid = (CharSequence)value$; break;
    case 2: oid = (CharSequence)value$; break;
    case 3: did = (CharSequence)value$; break;
    case 4: tripper_id = (CharSequence)value$; break;
    case 5: is_login = (CharSequence)value$; break;
    case 6: from = (CharSequence)value$; break;
    case 7: type = (CharSequence)value$; break;
    case 8: status = (CharSequence)value$; break;
    case 9: phonesystem = (CharSequence)value$; break;
    case 10: phone = (CharSequence)value$; break;
    case 11: phone_system_version = (CharSequence)value$; break;
    case 12: app_version = (CharSequence)value$; break;
    case 13: time = (CharSequence)value$; break;
    case 14: all = (CharSequence)value$; break;
    case 15: day = (Integer)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'ip' field.
   * @return The value of the 'ip' field.
   */
  public CharSequence getIp() {
    return ip;
  }

  /**
   * Sets the value of the 'ip' field.
   * @param value the value to set.
   */
  public void setIp(CharSequence value) {
    this.ip = value;
  }

  /**
   * Gets the value of the 'uid' field.
   * @return The value of the 'uid' field.
   */
  public CharSequence getUid() {
    return uid;
  }

  /**
   * Sets the value of the 'uid' field.
   * @param value the value to set.
   */
  public void setUid(CharSequence value) {
    this.uid = value;
  }

  /**
   * Gets the value of the 'oid' field.
   * @return The value of the 'oid' field.
   */
  public CharSequence getOid() {
    return oid;
  }

  /**
   * Sets the value of the 'oid' field.
   * @param value the value to set.
   */
  public void setOid(CharSequence value) {
    this.oid = value;
  }

  /**
   * Gets the value of the 'did' field.
   * @return The value of the 'did' field.
   */
  public CharSequence getDid() {
    return did;
  }

  /**
   * Sets the value of the 'did' field.
   * @param value the value to set.
   */
  public void setDid(CharSequence value) {
    this.did = value;
  }

  /**
   * Gets the value of the 'tripper_id' field.
   * @return The value of the 'tripper_id' field.
   */
  public CharSequence getTripperId() {
    return tripper_id;
  }

  /**
   * Sets the value of the 'tripper_id' field.
   * @param value the value to set.
   */
  public void setTripperId(CharSequence value) {
    this.tripper_id = value;
  }

  /**
   * Gets the value of the 'is_login' field.
   * @return The value of the 'is_login' field.
   */
  public CharSequence getIsLogin() {
    return is_login;
  }

  /**
   * Sets the value of the 'is_login' field.
   * @param value the value to set.
   */
  public void setIsLogin(CharSequence value) {
    this.is_login = value;
  }

  /**
   * Gets the value of the 'from' field.
   * @return The value of the 'from' field.
   */
  public CharSequence getFrom() {
    return from;
  }

  /**
   * Sets the value of the 'from' field.
   * @param value the value to set.
   */
  public void setFrom(CharSequence value) {
    this.from = value;
  }

  /**
   * Gets the value of the 'type' field.
   * @return The value of the 'type' field.
   */
  public CharSequence getType() {
    return type;
  }

  /**
   * Sets the value of the 'type' field.
   * @param value the value to set.
   */
  public void setType(CharSequence value) {
    this.type = value;
  }

  /**
   * Gets the value of the 'status' field.
   * @return The value of the 'status' field.
   */
  public CharSequence getStatus() {
    return status;
  }

  /**
   * Sets the value of the 'status' field.
   * @param value the value to set.
   */
  public void setStatus(CharSequence value) {
    this.status = value;
  }

  /**
   * Gets the value of the 'phonesystem' field.
   * @return The value of the 'phonesystem' field.
   */
  public CharSequence getPhonesystem() {
    return phonesystem;
  }

  /**
   * Sets the value of the 'phonesystem' field.
   * @param value the value to set.
   */
  public void setPhonesystem(CharSequence value) {
    this.phonesystem = value;
  }

  /**
   * Gets the value of the 'phone' field.
   * @return The value of the 'phone' field.
   */
  public CharSequence getPhone() {
    return phone;
  }

  /**
   * Sets the value of the 'phone' field.
   * @param value the value to set.
   */
  public void setPhone(CharSequence value) {
    this.phone = value;
  }

  /**
   * Gets the value of the 'phone_system_version' field.
   * @return The value of the 'phone_system_version' field.
   */
  public CharSequence getPhoneSystemVersion() {
    return phone_system_version;
  }

  /**
   * Sets the value of the 'phone_system_version' field.
   * @param value the value to set.
   */
  public void setPhoneSystemVersion(CharSequence value) {
    this.phone_system_version = value;
  }

  /**
   * Gets the value of the 'app_version' field.
   * @return The value of the 'app_version' field.
   */
  public CharSequence getAppVersion() {
    return app_version;
  }

  /**
   * Sets the value of the 'app_version' field.
   * @param value the value to set.
   */
  public void setAppVersion(CharSequence value) {
    this.app_version = value;
  }

  /**
   * Gets the value of the 'time' field.
   * @return The value of the 'time' field.
   */
  public CharSequence getTime() {
    return time;
  }

  /**
   * Sets the value of the 'time' field.
   * @param value the value to set.
   */
  public void setTime(CharSequence value) {
    this.time = value;
  }

  /**
   * Gets the value of the 'all' field.
   * @return The value of the 'all' field.
   */
  public CharSequence getAll() {
    return all;
  }

  /**
   * Sets the value of the 'all' field.
   * @param value the value to set.
   */
  public void setAll(CharSequence value) {
    this.all = value;
  }

  /**
   * Gets the value of the 'day' field.
   * @return The value of the 'day' field.
   */
  public Integer getDay() {
    return day;
  }

  /**
   * Sets the value of the 'day' field.
   * @param value the value to set.
   */
  public void setDay(Integer value) {
    this.day = value;
  }

  /**
   * Creates a new PvLog RecordBuilder.
   * @return A new PvLog RecordBuilder
   */
  public static PvLog.Builder newBuilder() {
    return new PvLog.Builder();
  }

  /**
   * Creates a new PvLog RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new PvLog RecordBuilder
   */
  public static PvLog.Builder newBuilder(PvLog.Builder other) {
    return new PvLog.Builder(other);
  }

  /**
   * Creates a new PvLog RecordBuilder by copying an existing PvLog instance.
   * @param other The existing instance to copy.
   * @return A new PvLog RecordBuilder
   */
  public static PvLog.Builder newBuilder(PvLog other) {
    return new PvLog.Builder(other);
  }

  /**
   * RecordBuilder for PvLog instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<PvLog>
    implements org.apache.avro.data.RecordBuilder<PvLog> {

    private CharSequence ip;
    private CharSequence uid;
    private CharSequence oid;
    private CharSequence did;
    private CharSequence tripper_id;
    private CharSequence is_login;
    private CharSequence from;
    private CharSequence type;
    private CharSequence status;
    private CharSequence phonesystem;
    private CharSequence phone;
    private CharSequence phone_system_version;
    private CharSequence app_version;
    private CharSequence time;
    private CharSequence all;
    private int day;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(PvLog.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.ip)) {
        this.ip = data().deepCopy(fields()[0].schema(), other.ip);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.uid)) {
        this.uid = data().deepCopy(fields()[1].schema(), other.uid);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.oid)) {
        this.oid = data().deepCopy(fields()[2].schema(), other.oid);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.did)) {
        this.did = data().deepCopy(fields()[3].schema(), other.did);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.tripper_id)) {
        this.tripper_id = data().deepCopy(fields()[4].schema(), other.tripper_id);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.is_login)) {
        this.is_login = data().deepCopy(fields()[5].schema(), other.is_login);
        fieldSetFlags()[5] = true;
      }
      if (isValidValue(fields()[6], other.from)) {
        this.from = data().deepCopy(fields()[6].schema(), other.from);
        fieldSetFlags()[6] = true;
      }
      if (isValidValue(fields()[7], other.type)) {
        this.type = data().deepCopy(fields()[7].schema(), other.type);
        fieldSetFlags()[7] = true;
      }
      if (isValidValue(fields()[8], other.status)) {
        this.status = data().deepCopy(fields()[8].schema(), other.status);
        fieldSetFlags()[8] = true;
      }
      if (isValidValue(fields()[9], other.phonesystem)) {
        this.phonesystem = data().deepCopy(fields()[9].schema(), other.phonesystem);
        fieldSetFlags()[9] = true;
      }
      if (isValidValue(fields()[10], other.phone)) {
        this.phone = data().deepCopy(fields()[10].schema(), other.phone);
        fieldSetFlags()[10] = true;
      }
      if (isValidValue(fields()[11], other.phone_system_version)) {
        this.phone_system_version = data().deepCopy(fields()[11].schema(), other.phone_system_version);
        fieldSetFlags()[11] = true;
      }
      if (isValidValue(fields()[12], other.app_version)) {
        this.app_version = data().deepCopy(fields()[12].schema(), other.app_version);
        fieldSetFlags()[12] = true;
      }
      if (isValidValue(fields()[13], other.time)) {
        this.time = data().deepCopy(fields()[13].schema(), other.time);
        fieldSetFlags()[13] = true;
      }
      if (isValidValue(fields()[14], other.all)) {
        this.all = data().deepCopy(fields()[14].schema(), other.all);
        fieldSetFlags()[14] = true;
      }
      if (isValidValue(fields()[15], other.day)) {
        this.day = data().deepCopy(fields()[15].schema(), other.day);
        fieldSetFlags()[15] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing PvLog instance
     * @param other The existing instance to copy.
     */
    private Builder(PvLog other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.ip)) {
        this.ip = data().deepCopy(fields()[0].schema(), other.ip);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.uid)) {
        this.uid = data().deepCopy(fields()[1].schema(), other.uid);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.oid)) {
        this.oid = data().deepCopy(fields()[2].schema(), other.oid);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.did)) {
        this.did = data().deepCopy(fields()[3].schema(), other.did);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.tripper_id)) {
        this.tripper_id = data().deepCopy(fields()[4].schema(), other.tripper_id);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.is_login)) {
        this.is_login = data().deepCopy(fields()[5].schema(), other.is_login);
        fieldSetFlags()[5] = true;
      }
      if (isValidValue(fields()[6], other.from)) {
        this.from = data().deepCopy(fields()[6].schema(), other.from);
        fieldSetFlags()[6] = true;
      }
      if (isValidValue(fields()[7], other.type)) {
        this.type = data().deepCopy(fields()[7].schema(), other.type);
        fieldSetFlags()[7] = true;
      }
      if (isValidValue(fields()[8], other.status)) {
        this.status = data().deepCopy(fields()[8].schema(), other.status);
        fieldSetFlags()[8] = true;
      }
      if (isValidValue(fields()[9], other.phonesystem)) {
        this.phonesystem = data().deepCopy(fields()[9].schema(), other.phonesystem);
        fieldSetFlags()[9] = true;
      }
      if (isValidValue(fields()[10], other.phone)) {
        this.phone = data().deepCopy(fields()[10].schema(), other.phone);
        fieldSetFlags()[10] = true;
      }
      if (isValidValue(fields()[11], other.phone_system_version)) {
        this.phone_system_version = data().deepCopy(fields()[11].schema(), other.phone_system_version);
        fieldSetFlags()[11] = true;
      }
      if (isValidValue(fields()[12], other.app_version)) {
        this.app_version = data().deepCopy(fields()[12].schema(), other.app_version);
        fieldSetFlags()[12] = true;
      }
      if (isValidValue(fields()[13], other.time)) {
        this.time = data().deepCopy(fields()[13].schema(), other.time);
        fieldSetFlags()[13] = true;
      }
      if (isValidValue(fields()[14], other.all)) {
        this.all = data().deepCopy(fields()[14].schema(), other.all);
        fieldSetFlags()[14] = true;
      }
      if (isValidValue(fields()[15], other.day)) {
        this.day = data().deepCopy(fields()[15].schema(), other.day);
        fieldSetFlags()[15] = true;
      }
    }

    /**
      * Gets the value of the 'ip' field.
      * @return The value.
      */
    public CharSequence getIp() {
      return ip;
    }

    /**
      * Sets the value of the 'ip' field.
      * @param value The value of 'ip'.
      * @return This builder.
      */
    public PvLog.Builder setIp(CharSequence value) {
      validate(fields()[0], value);
      this.ip = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'ip' field has been set.
      * @return True if the 'ip' field has been set, false otherwise.
      */
    public boolean hasIp() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'ip' field.
      * @return This builder.
      */
    public PvLog.Builder clearIp() {
      ip = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'uid' field.
      * @return The value.
      */
    public CharSequence getUid() {
      return uid;
    }

    /**
      * Sets the value of the 'uid' field.
      * @param value The value of 'uid'.
      * @return This builder.
      */
    public PvLog.Builder setUid(CharSequence value) {
      validate(fields()[1], value);
      this.uid = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'uid' field has been set.
      * @return True if the 'uid' field has been set, false otherwise.
      */
    public boolean hasUid() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'uid' field.
      * @return This builder.
      */
    public PvLog.Builder clearUid() {
      uid = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'oid' field.
      * @return The value.
      */
    public CharSequence getOid() {
      return oid;
    }

    /**
      * Sets the value of the 'oid' field.
      * @param value The value of 'oid'.
      * @return This builder.
      */
    public PvLog.Builder setOid(CharSequence value) {
      validate(fields()[2], value);
      this.oid = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'oid' field has been set.
      * @return True if the 'oid' field has been set, false otherwise.
      */
    public boolean hasOid() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'oid' field.
      * @return This builder.
      */
    public PvLog.Builder clearOid() {
      oid = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'did' field.
      * @return The value.
      */
    public CharSequence getDid() {
      return did;
    }

    /**
      * Sets the value of the 'did' field.
      * @param value The value of 'did'.
      * @return This builder.
      */
    public PvLog.Builder setDid(CharSequence value) {
      validate(fields()[3], value);
      this.did = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'did' field has been set.
      * @return True if the 'did' field has been set, false otherwise.
      */
    public boolean hasDid() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'did' field.
      * @return This builder.
      */
    public PvLog.Builder clearDid() {
      did = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /**
      * Gets the value of the 'tripper_id' field.
      * @return The value.
      */
    public CharSequence getTripperId() {
      return tripper_id;
    }

    /**
      * Sets the value of the 'tripper_id' field.
      * @param value The value of 'tripper_id'.
      * @return This builder.
      */
    public PvLog.Builder setTripperId(CharSequence value) {
      validate(fields()[4], value);
      this.tripper_id = value;
      fieldSetFlags()[4] = true;
      return this;
    }

    /**
      * Checks whether the 'tripper_id' field has been set.
      * @return True if the 'tripper_id' field has been set, false otherwise.
      */
    public boolean hasTripperId() {
      return fieldSetFlags()[4];
    }


    /**
      * Clears the value of the 'tripper_id' field.
      * @return This builder.
      */
    public PvLog.Builder clearTripperId() {
      tripper_id = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /**
      * Gets the value of the 'is_login' field.
      * @return The value.
      */
    public CharSequence getIsLogin() {
      return is_login;
    }

    /**
      * Sets the value of the 'is_login' field.
      * @param value The value of 'is_login'.
      * @return This builder.
      */
    public PvLog.Builder setIsLogin(CharSequence value) {
      validate(fields()[5], value);
      this.is_login = value;
      fieldSetFlags()[5] = true;
      return this;
    }

    /**
      * Checks whether the 'is_login' field has been set.
      * @return True if the 'is_login' field has been set, false otherwise.
      */
    public boolean hasIsLogin() {
      return fieldSetFlags()[5];
    }


    /**
      * Clears the value of the 'is_login' field.
      * @return This builder.
      */
    public PvLog.Builder clearIsLogin() {
      is_login = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    /**
      * Gets the value of the 'from' field.
      * @return The value.
      */
    public CharSequence getFrom() {
      return from;
    }

    /**
      * Sets the value of the 'from' field.
      * @param value The value of 'from'.
      * @return This builder.
      */
    public PvLog.Builder setFrom(CharSequence value) {
      validate(fields()[6], value);
      this.from = value;
      fieldSetFlags()[6] = true;
      return this;
    }

    /**
      * Checks whether the 'from' field has been set.
      * @return True if the 'from' field has been set, false otherwise.
      */
    public boolean hasFrom() {
      return fieldSetFlags()[6];
    }


    /**
      * Clears the value of the 'from' field.
      * @return This builder.
      */
    public PvLog.Builder clearFrom() {
      from = null;
      fieldSetFlags()[6] = false;
      return this;
    }

    /**
      * Gets the value of the 'type' field.
      * @return The value.
      */
    public CharSequence getType() {
      return type;
    }

    /**
      * Sets the value of the 'type' field.
      * @param value The value of 'type'.
      * @return This builder.
      */
    public PvLog.Builder setType(CharSequence value) {
      validate(fields()[7], value);
      this.type = value;
      fieldSetFlags()[7] = true;
      return this;
    }

    /**
      * Checks whether the 'type' field has been set.
      * @return True if the 'type' field has been set, false otherwise.
      */
    public boolean hasType() {
      return fieldSetFlags()[7];
    }


    /**
      * Clears the value of the 'type' field.
      * @return This builder.
      */
    public PvLog.Builder clearType() {
      type = null;
      fieldSetFlags()[7] = false;
      return this;
    }

    /**
      * Gets the value of the 'status' field.
      * @return The value.
      */
    public CharSequence getStatus() {
      return status;
    }

    /**
      * Sets the value of the 'status' field.
      * @param value The value of 'status'.
      * @return This builder.
      */
    public PvLog.Builder setStatus(CharSequence value) {
      validate(fields()[8], value);
      this.status = value;
      fieldSetFlags()[8] = true;
      return this;
    }

    /**
      * Checks whether the 'status' field has been set.
      * @return True if the 'status' field has been set, false otherwise.
      */
    public boolean hasStatus() {
      return fieldSetFlags()[8];
    }


    /**
      * Clears the value of the 'status' field.
      * @return This builder.
      */
    public PvLog.Builder clearStatus() {
      status = null;
      fieldSetFlags()[8] = false;
      return this;
    }

    /**
      * Gets the value of the 'phonesystem' field.
      * @return The value.
      */
    public CharSequence getPhonesystem() {
      return phonesystem;
    }

    /**
      * Sets the value of the 'phonesystem' field.
      * @param value The value of 'phonesystem'.
      * @return This builder.
      */
    public PvLog.Builder setPhonesystem(CharSequence value) {
      validate(fields()[9], value);
      this.phonesystem = value;
      fieldSetFlags()[9] = true;
      return this;
    }

    /**
      * Checks whether the 'phonesystem' field has been set.
      * @return True if the 'phonesystem' field has been set, false otherwise.
      */
    public boolean hasPhonesystem() {
      return fieldSetFlags()[9];
    }


    /**
      * Clears the value of the 'phonesystem' field.
      * @return This builder.
      */
    public PvLog.Builder clearPhonesystem() {
      phonesystem = null;
      fieldSetFlags()[9] = false;
      return this;
    }

    /**
      * Gets the value of the 'phone' field.
      * @return The value.
      */
    public CharSequence getPhone() {
      return phone;
    }

    /**
      * Sets the value of the 'phone' field.
      * @param value The value of 'phone'.
      * @return This builder.
      */
    public PvLog.Builder setPhone(CharSequence value) {
      validate(fields()[10], value);
      this.phone = value;
      fieldSetFlags()[10] = true;
      return this;
    }

    /**
      * Checks whether the 'phone' field has been set.
      * @return True if the 'phone' field has been set, false otherwise.
      */
    public boolean hasPhone() {
      return fieldSetFlags()[10];
    }


    /**
      * Clears the value of the 'phone' field.
      * @return This builder.
      */
    public PvLog.Builder clearPhone() {
      phone = null;
      fieldSetFlags()[10] = false;
      return this;
    }

    /**
      * Gets the value of the 'phone_system_version' field.
      * @return The value.
      */
    public CharSequence getPhoneSystemVersion() {
      return phone_system_version;
    }

    /**
      * Sets the value of the 'phone_system_version' field.
      * @param value The value of 'phone_system_version'.
      * @return This builder.
      */
    public PvLog.Builder setPhoneSystemVersion(CharSequence value) {
      validate(fields()[11], value);
      this.phone_system_version = value;
      fieldSetFlags()[11] = true;
      return this;
    }

    /**
      * Checks whether the 'phone_system_version' field has been set.
      * @return True if the 'phone_system_version' field has been set, false otherwise.
      */
    public boolean hasPhoneSystemVersion() {
      return fieldSetFlags()[11];
    }


    /**
      * Clears the value of the 'phone_system_version' field.
      * @return This builder.
      */
    public PvLog.Builder clearPhoneSystemVersion() {
      phone_system_version = null;
      fieldSetFlags()[11] = false;
      return this;
    }

    /**
      * Gets the value of the 'app_version' field.
      * @return The value.
      */
    public CharSequence getAppVersion() {
      return app_version;
    }

    /**
      * Sets the value of the 'app_version' field.
      * @param value The value of 'app_version'.
      * @return This builder.
      */
    public PvLog.Builder setAppVersion(CharSequence value) {
      validate(fields()[12], value);
      this.app_version = value;
      fieldSetFlags()[12] = true;
      return this;
    }

    /**
      * Checks whether the 'app_version' field has been set.
      * @return True if the 'app_version' field has been set, false otherwise.
      */
    public boolean hasAppVersion() {
      return fieldSetFlags()[12];
    }


    /**
      * Clears the value of the 'app_version' field.
      * @return This builder.
      */
    public PvLog.Builder clearAppVersion() {
      app_version = null;
      fieldSetFlags()[12] = false;
      return this;
    }

    /**
      * Gets the value of the 'time' field.
      * @return The value.
      */
    public CharSequence getTime() {
      return time;
    }

    /**
      * Sets the value of the 'time' field.
      * @param value The value of 'time'.
      * @return This builder.
      */
    public PvLog.Builder setTime(CharSequence value) {
      validate(fields()[13], value);
      this.time = value;
      fieldSetFlags()[13] = true;
      return this;
    }

    /**
      * Checks whether the 'time' field has been set.
      * @return True if the 'time' field has been set, false otherwise.
      */
    public boolean hasTime() {
      return fieldSetFlags()[13];
    }


    /**
      * Clears the value of the 'time' field.
      * @return This builder.
      */
    public PvLog.Builder clearTime() {
      time = null;
      fieldSetFlags()[13] = false;
      return this;
    }

    /**
      * Gets the value of the 'all' field.
      * @return The value.
      */
    public CharSequence getAll() {
      return all;
    }

    /**
      * Sets the value of the 'all' field.
      * @param value The value of 'all'.
      * @return This builder.
      */
    public PvLog.Builder setAll(CharSequence value) {
      validate(fields()[14], value);
      this.all = value;
      fieldSetFlags()[14] = true;
      return this;
    }

    /**
      * Checks whether the 'all' field has been set.
      * @return True if the 'all' field has been set, false otherwise.
      */
    public boolean hasAll() {
      return fieldSetFlags()[14];
    }


    /**
      * Clears the value of the 'all' field.
      * @return This builder.
      */
    public PvLog.Builder clearAll() {
      all = null;
      fieldSetFlags()[14] = false;
      return this;
    }

    /**
      * Gets the value of the 'day' field.
      * @return The value.
      */
    public Integer getDay() {
      return day;
    }

    /**
      * Sets the value of the 'day' field.
      * @param value The value of 'day'.
      * @return This builder.
      */
    public PvLog.Builder setDay(int value) {
      validate(fields()[15], value);
      this.day = value;
      fieldSetFlags()[15] = true;
      return this;
    }

    /**
      * Checks whether the 'day' field has been set.
      * @return True if the 'day' field has been set, false otherwise.
      */
    public boolean hasDay() {
      return fieldSetFlags()[15];
    }


    /**
      * Clears the value of the 'day' field.
      * @return This builder.
      */
    public PvLog.Builder clearDay() {
      fieldSetFlags()[15] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PvLog build() {
      try {
        PvLog record = new PvLog();
        record.ip = fieldSetFlags()[0] ? this.ip : (CharSequence) defaultValue(fields()[0]);
        record.uid = fieldSetFlags()[1] ? this.uid : (CharSequence) defaultValue(fields()[1]);
        record.oid = fieldSetFlags()[2] ? this.oid : (CharSequence) defaultValue(fields()[2]);
        record.did = fieldSetFlags()[3] ? this.did : (CharSequence) defaultValue(fields()[3]);
        record.tripper_id = fieldSetFlags()[4] ? this.tripper_id : (CharSequence) defaultValue(fields()[4]);
        record.is_login = fieldSetFlags()[5] ? this.is_login : (CharSequence) defaultValue(fields()[5]);
        record.from = fieldSetFlags()[6] ? this.from : (CharSequence) defaultValue(fields()[6]);
        record.type = fieldSetFlags()[7] ? this.type : (CharSequence) defaultValue(fields()[7]);
        record.status = fieldSetFlags()[8] ? this.status : (CharSequence) defaultValue(fields()[8]);
        record.phonesystem = fieldSetFlags()[9] ? this.phonesystem : (CharSequence) defaultValue(fields()[9]);
        record.phone = fieldSetFlags()[10] ? this.phone : (CharSequence) defaultValue(fields()[10]);
        record.phone_system_version = fieldSetFlags()[11] ? this.phone_system_version : (CharSequence) defaultValue(fields()[11]);
        record.app_version = fieldSetFlags()[12] ? this.app_version : (CharSequence) defaultValue(fields()[12]);
        record.time = fieldSetFlags()[13] ? this.time : (CharSequence) defaultValue(fields()[13]);
        record.all = fieldSetFlags()[14] ? this.all : (CharSequence) defaultValue(fields()[14]);
        record.day = fieldSetFlags()[15] ? this.day : (Integer) defaultValue(fields()[15]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<PvLog>
    WRITER$ = (org.apache.avro.io.DatumWriter<PvLog>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<PvLog>
    READER$ = (org.apache.avro.io.DatumReader<PvLog>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}
