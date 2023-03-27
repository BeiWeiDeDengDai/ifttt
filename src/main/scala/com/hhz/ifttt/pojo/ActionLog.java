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
public class ActionLog extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 1657138491759353888L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ActionLog\",\"namespace\":\"com.hhz.ifttt.pojo\",\"fields\":[{\"name\":\"time\",\"type\":\"long\"},{\"name\":\"ip\",\"type\":\"string\"},{\"name\":\"url\",\"type\":\"string\"},{\"name\":\"type\",\"type\":\"string\"},{\"name\":\"uid\",\"type\":\"string\"},{\"name\":\"params\",\"type\":\"string\"},{\"name\":\"user_agent\",\"type\":\"string\"},{\"name\":\"did\",\"type\":\"string\"},{\"name\":\"author_uid\",\"type\":\"string\"},{\"name\":\"day\",\"type\":\"int\"},{\"name\":\"source\",\"type\":\"string\"},{\"name\":\"beat_hostname\",\"type\":\"string\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<ActionLog> ENCODER =
      new BinaryMessageEncoder<ActionLog>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<ActionLog> DECODER =
      new BinaryMessageDecoder<ActionLog>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   */
  public static BinaryMessageDecoder<ActionLog> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   */
  public static BinaryMessageDecoder<ActionLog> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<ActionLog>(MODEL$, SCHEMA$, resolver);
  }

  /** Serializes this ActionLog to a ByteBuffer. */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /** Deserializes a ActionLog from a ByteBuffer. */
  public static ActionLog fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  @Deprecated public long time;
  @Deprecated public CharSequence ip;
  @Deprecated public CharSequence url;
  @Deprecated public CharSequence type;
  @Deprecated public CharSequence uid;
  @Deprecated public CharSequence params;
  @Deprecated public CharSequence user_agent;
  @Deprecated public CharSequence did;
  @Deprecated public CharSequence author_uid;
  @Deprecated public int day;
  @Deprecated public CharSequence source;
  @Deprecated public CharSequence beat_hostname;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public ActionLog() {}

  /**
   * All-args constructor.
   * @param time The new value for time
   * @param ip The new value for ip
   * @param url The new value for url
   * @param type The new value for type
   * @param uid The new value for uid
   * @param params The new value for params
   * @param user_agent The new value for user_agent
   * @param did The new value for did
   * @param author_uid The new value for author_uid
   * @param day The new value for day
   * @param source The new value for source
   * @param beat_hostname The new value for beat_hostname
   */
  public ActionLog(Long time, CharSequence ip, CharSequence url, CharSequence type, CharSequence uid, CharSequence params, CharSequence user_agent, CharSequence did, CharSequence author_uid, Integer day, CharSequence source, CharSequence beat_hostname) {
    this.time = time;
    this.ip = ip;
    this.url = url;
    this.type = type;
    this.uid = uid;
    this.params = params;
    this.user_agent = user_agent;
    this.did = did;
    this.author_uid = author_uid;
    this.day = day;
    this.source = source;
    this.beat_hostname = beat_hostname;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public Object get(int field$) {
    switch (field$) {
    case 0: return time;
    case 1: return ip;
    case 2: return url;
    case 3: return type;
    case 4: return uid;
    case 5: return params;
    case 6: return user_agent;
    case 7: return did;
    case 8: return author_uid;
    case 9: return day;
    case 10: return source;
    case 11: return beat_hostname;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, Object value$) {
    switch (field$) {
    case 0: time = (Long)value$; break;
    case 1: ip = (CharSequence)value$; break;
    case 2: url = (CharSequence)value$; break;
    case 3: type = (CharSequence)value$; break;
    case 4: uid = (CharSequence)value$; break;
    case 5: params = (CharSequence)value$; break;
    case 6: user_agent = (CharSequence)value$; break;
    case 7: did = (CharSequence)value$; break;
    case 8: author_uid = (CharSequence)value$; break;
    case 9: day = (Integer)value$; break;
    case 10: source = (CharSequence)value$; break;
    case 11: beat_hostname = (CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'time' field.
   * @return The value of the 'time' field.
   */
  public Long getTime() {
    return time;
  }

  /**
   * Sets the value of the 'time' field.
   * @param value the value to set.
   */
  public void setTime(Long value) {
    this.time = value;
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
   * Gets the value of the 'url' field.
   * @return The value of the 'url' field.
   */
  public CharSequence getUrl() {
    return url;
  }

  /**
   * Sets the value of the 'url' field.
   * @param value the value to set.
   */
  public void setUrl(CharSequence value) {
    this.url = value;
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
   * Gets the value of the 'params' field.
   * @return The value of the 'params' field.
   */
  public CharSequence getParams() {
    return params;
  }

  /**
   * Sets the value of the 'params' field.
   * @param value the value to set.
   */
  public void setParams(CharSequence value) {
    this.params = value;
  }

  /**
   * Gets the value of the 'user_agent' field.
   * @return The value of the 'user_agent' field.
   */
  public CharSequence getUserAgent() {
    return user_agent;
  }

  /**
   * Sets the value of the 'user_agent' field.
   * @param value the value to set.
   */
  public void setUserAgent(CharSequence value) {
    this.user_agent = value;
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
   * Gets the value of the 'author_uid' field.
   * @return The value of the 'author_uid' field.
   */
  public CharSequence getAuthorUid() {
    return author_uid;
  }

  /**
   * Sets the value of the 'author_uid' field.
   * @param value the value to set.
   */
  public void setAuthorUid(CharSequence value) {
    this.author_uid = value;
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
   * Gets the value of the 'source' field.
   * @return The value of the 'source' field.
   */
  public CharSequence getSource() {
    return source;
  }

  /**
   * Sets the value of the 'source' field.
   * @param value the value to set.
   */
  public void setSource(CharSequence value) {
    this.source = value;
  }

  /**
   * Gets the value of the 'beat_hostname' field.
   * @return The value of the 'beat_hostname' field.
   */
  public CharSequence getBeatHostname() {
    return beat_hostname;
  }

  /**
   * Sets the value of the 'beat_hostname' field.
   * @param value the value to set.
   */
  public void setBeatHostname(CharSequence value) {
    this.beat_hostname = value;
  }

  /**
   * Creates a new ActionLog RecordBuilder.
   * @return A new ActionLog RecordBuilder
   */
  public static ActionLog.Builder newBuilder() {
    return new ActionLog.Builder();
  }

  /**
   * Creates a new ActionLog RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new ActionLog RecordBuilder
   */
  public static ActionLog.Builder newBuilder(ActionLog.Builder other) {
    return new ActionLog.Builder(other);
  }

  /**
   * Creates a new ActionLog RecordBuilder by copying an existing ActionLog instance.
   * @param other The existing instance to copy.
   * @return A new ActionLog RecordBuilder
   */
  public static ActionLog.Builder newBuilder(ActionLog other) {
    return new ActionLog.Builder(other);
  }

  /**
   * RecordBuilder for ActionLog instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<ActionLog>
    implements org.apache.avro.data.RecordBuilder<ActionLog> {

    private long time;
    private CharSequence ip;
    private CharSequence url;
    private CharSequence type;
    private CharSequence uid;
    private CharSequence params;
    private CharSequence user_agent;
    private CharSequence did;
    private CharSequence author_uid;
    private int day;
    private CharSequence source;
    private CharSequence beat_hostname;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(ActionLog.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.time)) {
        this.time = data().deepCopy(fields()[0].schema(), other.time);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.ip)) {
        this.ip = data().deepCopy(fields()[1].schema(), other.ip);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.url)) {
        this.url = data().deepCopy(fields()[2].schema(), other.url);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.type)) {
        this.type = data().deepCopy(fields()[3].schema(), other.type);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.uid)) {
        this.uid = data().deepCopy(fields()[4].schema(), other.uid);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.params)) {
        this.params = data().deepCopy(fields()[5].schema(), other.params);
        fieldSetFlags()[5] = true;
      }
      if (isValidValue(fields()[6], other.user_agent)) {
        this.user_agent = data().deepCopy(fields()[6].schema(), other.user_agent);
        fieldSetFlags()[6] = true;
      }
      if (isValidValue(fields()[7], other.did)) {
        this.did = data().deepCopy(fields()[7].schema(), other.did);
        fieldSetFlags()[7] = true;
      }
      if (isValidValue(fields()[8], other.author_uid)) {
        this.author_uid = data().deepCopy(fields()[8].schema(), other.author_uid);
        fieldSetFlags()[8] = true;
      }
      if (isValidValue(fields()[9], other.day)) {
        this.day = data().deepCopy(fields()[9].schema(), other.day);
        fieldSetFlags()[9] = true;
      }
      if (isValidValue(fields()[10], other.source)) {
        this.source = data().deepCopy(fields()[10].schema(), other.source);
        fieldSetFlags()[10] = true;
      }
      if (isValidValue(fields()[11], other.beat_hostname)) {
        this.beat_hostname = data().deepCopy(fields()[11].schema(), other.beat_hostname);
        fieldSetFlags()[11] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing ActionLog instance
     * @param other The existing instance to copy.
     */
    private Builder(ActionLog other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.time)) {
        this.time = data().deepCopy(fields()[0].schema(), other.time);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.ip)) {
        this.ip = data().deepCopy(fields()[1].schema(), other.ip);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.url)) {
        this.url = data().deepCopy(fields()[2].schema(), other.url);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.type)) {
        this.type = data().deepCopy(fields()[3].schema(), other.type);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.uid)) {
        this.uid = data().deepCopy(fields()[4].schema(), other.uid);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.params)) {
        this.params = data().deepCopy(fields()[5].schema(), other.params);
        fieldSetFlags()[5] = true;
      }
      if (isValidValue(fields()[6], other.user_agent)) {
        this.user_agent = data().deepCopy(fields()[6].schema(), other.user_agent);
        fieldSetFlags()[6] = true;
      }
      if (isValidValue(fields()[7], other.did)) {
        this.did = data().deepCopy(fields()[7].schema(), other.did);
        fieldSetFlags()[7] = true;
      }
      if (isValidValue(fields()[8], other.author_uid)) {
        this.author_uid = data().deepCopy(fields()[8].schema(), other.author_uid);
        fieldSetFlags()[8] = true;
      }
      if (isValidValue(fields()[9], other.day)) {
        this.day = data().deepCopy(fields()[9].schema(), other.day);
        fieldSetFlags()[9] = true;
      }
      if (isValidValue(fields()[10], other.source)) {
        this.source = data().deepCopy(fields()[10].schema(), other.source);
        fieldSetFlags()[10] = true;
      }
      if (isValidValue(fields()[11], other.beat_hostname)) {
        this.beat_hostname = data().deepCopy(fields()[11].schema(), other.beat_hostname);
        fieldSetFlags()[11] = true;
      }
    }

    /**
      * Gets the value of the 'time' field.
      * @return The value.
      */
    public Long getTime() {
      return time;
    }

    /**
      * Sets the value of the 'time' field.
      * @param value The value of 'time'.
      * @return This builder.
      */
    public ActionLog.Builder setTime(long value) {
      validate(fields()[0], value);
      this.time = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'time' field has been set.
      * @return True if the 'time' field has been set, false otherwise.
      */
    public boolean hasTime() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'time' field.
      * @return This builder.
      */
    public ActionLog.Builder clearTime() {
      fieldSetFlags()[0] = false;
      return this;
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
    public ActionLog.Builder setIp(CharSequence value) {
      validate(fields()[1], value);
      this.ip = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'ip' field has been set.
      * @return True if the 'ip' field has been set, false otherwise.
      */
    public boolean hasIp() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'ip' field.
      * @return This builder.
      */
    public ActionLog.Builder clearIp() {
      ip = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'url' field.
      * @return The value.
      */
    public CharSequence getUrl() {
      return url;
    }

    /**
      * Sets the value of the 'url' field.
      * @param value The value of 'url'.
      * @return This builder.
      */
    public ActionLog.Builder setUrl(CharSequence value) {
      validate(fields()[2], value);
      this.url = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'url' field has been set.
      * @return True if the 'url' field has been set, false otherwise.
      */
    public boolean hasUrl() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'url' field.
      * @return This builder.
      */
    public ActionLog.Builder clearUrl() {
      url = null;
      fieldSetFlags()[2] = false;
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
    public ActionLog.Builder setType(CharSequence value) {
      validate(fields()[3], value);
      this.type = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'type' field has been set.
      * @return True if the 'type' field has been set, false otherwise.
      */
    public boolean hasType() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'type' field.
      * @return This builder.
      */
    public ActionLog.Builder clearType() {
      type = null;
      fieldSetFlags()[3] = false;
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
    public ActionLog.Builder setUid(CharSequence value) {
      validate(fields()[4], value);
      this.uid = value;
      fieldSetFlags()[4] = true;
      return this;
    }

    /**
      * Checks whether the 'uid' field has been set.
      * @return True if the 'uid' field has been set, false otherwise.
      */
    public boolean hasUid() {
      return fieldSetFlags()[4];
    }


    /**
      * Clears the value of the 'uid' field.
      * @return This builder.
      */
    public ActionLog.Builder clearUid() {
      uid = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /**
      * Gets the value of the 'params' field.
      * @return The value.
      */
    public CharSequence getParams() {
      return params;
    }

    /**
      * Sets the value of the 'params' field.
      * @param value The value of 'params'.
      * @return This builder.
      */
    public ActionLog.Builder setParams(CharSequence value) {
      validate(fields()[5], value);
      this.params = value;
      fieldSetFlags()[5] = true;
      return this;
    }

    /**
      * Checks whether the 'params' field has been set.
      * @return True if the 'params' field has been set, false otherwise.
      */
    public boolean hasParams() {
      return fieldSetFlags()[5];
    }


    /**
      * Clears the value of the 'params' field.
      * @return This builder.
      */
    public ActionLog.Builder clearParams() {
      params = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    /**
      * Gets the value of the 'user_agent' field.
      * @return The value.
      */
    public CharSequence getUserAgent() {
      return user_agent;
    }

    /**
      * Sets the value of the 'user_agent' field.
      * @param value The value of 'user_agent'.
      * @return This builder.
      */
    public ActionLog.Builder setUserAgent(CharSequence value) {
      validate(fields()[6], value);
      this.user_agent = value;
      fieldSetFlags()[6] = true;
      return this;
    }

    /**
      * Checks whether the 'user_agent' field has been set.
      * @return True if the 'user_agent' field has been set, false otherwise.
      */
    public boolean hasUserAgent() {
      return fieldSetFlags()[6];
    }


    /**
      * Clears the value of the 'user_agent' field.
      * @return This builder.
      */
    public ActionLog.Builder clearUserAgent() {
      user_agent = null;
      fieldSetFlags()[6] = false;
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
    public ActionLog.Builder setDid(CharSequence value) {
      validate(fields()[7], value);
      this.did = value;
      fieldSetFlags()[7] = true;
      return this;
    }

    /**
      * Checks whether the 'did' field has been set.
      * @return True if the 'did' field has been set, false otherwise.
      */
    public boolean hasDid() {
      return fieldSetFlags()[7];
    }


    /**
      * Clears the value of the 'did' field.
      * @return This builder.
      */
    public ActionLog.Builder clearDid() {
      did = null;
      fieldSetFlags()[7] = false;
      return this;
    }

    /**
      * Gets the value of the 'author_uid' field.
      * @return The value.
      */
    public CharSequence getAuthorUid() {
      return author_uid;
    }

    /**
      * Sets the value of the 'author_uid' field.
      * @param value The value of 'author_uid'.
      * @return This builder.
      */
    public ActionLog.Builder setAuthorUid(CharSequence value) {
      validate(fields()[8], value);
      this.author_uid = value;
      fieldSetFlags()[8] = true;
      return this;
    }

    /**
      * Checks whether the 'author_uid' field has been set.
      * @return True if the 'author_uid' field has been set, false otherwise.
      */
    public boolean hasAuthorUid() {
      return fieldSetFlags()[8];
    }


    /**
      * Clears the value of the 'author_uid' field.
      * @return This builder.
      */
    public ActionLog.Builder clearAuthorUid() {
      author_uid = null;
      fieldSetFlags()[8] = false;
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
    public ActionLog.Builder setDay(int value) {
      validate(fields()[9], value);
      this.day = value;
      fieldSetFlags()[9] = true;
      return this;
    }

    /**
      * Checks whether the 'day' field has been set.
      * @return True if the 'day' field has been set, false otherwise.
      */
    public boolean hasDay() {
      return fieldSetFlags()[9];
    }


    /**
      * Clears the value of the 'day' field.
      * @return This builder.
      */
    public ActionLog.Builder clearDay() {
      fieldSetFlags()[9] = false;
      return this;
    }

    /**
      * Gets the value of the 'source' field.
      * @return The value.
      */
    public CharSequence getSource() {
      return source;
    }

    /**
      * Sets the value of the 'source' field.
      * @param value The value of 'source'.
      * @return This builder.
      */
    public ActionLog.Builder setSource(CharSequence value) {
      validate(fields()[10], value);
      this.source = value;
      fieldSetFlags()[10] = true;
      return this;
    }

    /**
      * Checks whether the 'source' field has been set.
      * @return True if the 'source' field has been set, false otherwise.
      */
    public boolean hasSource() {
      return fieldSetFlags()[10];
    }


    /**
      * Clears the value of the 'source' field.
      * @return This builder.
      */
    public ActionLog.Builder clearSource() {
      source = null;
      fieldSetFlags()[10] = false;
      return this;
    }

    /**
      * Gets the value of the 'beat_hostname' field.
      * @return The value.
      */
    public CharSequence getBeatHostname() {
      return beat_hostname;
    }

    /**
      * Sets the value of the 'beat_hostname' field.
      * @param value The value of 'beat_hostname'.
      * @return This builder.
      */
    public ActionLog.Builder setBeatHostname(CharSequence value) {
      validate(fields()[11], value);
      this.beat_hostname = value;
      fieldSetFlags()[11] = true;
      return this;
    }

    /**
      * Checks whether the 'beat_hostname' field has been set.
      * @return True if the 'beat_hostname' field has been set, false otherwise.
      */
    public boolean hasBeatHostname() {
      return fieldSetFlags()[11];
    }


    /**
      * Clears the value of the 'beat_hostname' field.
      * @return This builder.
      */
    public ActionLog.Builder clearBeatHostname() {
      beat_hostname = null;
      fieldSetFlags()[11] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ActionLog build() {
      try {
        ActionLog record = new ActionLog();
        record.time = fieldSetFlags()[0] ? this.time : (Long) defaultValue(fields()[0]);
        record.ip = fieldSetFlags()[1] ? this.ip : (CharSequence) defaultValue(fields()[1]);
        record.url = fieldSetFlags()[2] ? this.url : (CharSequence) defaultValue(fields()[2]);
        record.type = fieldSetFlags()[3] ? this.type : (CharSequence) defaultValue(fields()[3]);
        record.uid = fieldSetFlags()[4] ? this.uid : (CharSequence) defaultValue(fields()[4]);
        record.params = fieldSetFlags()[5] ? this.params : (CharSequence) defaultValue(fields()[5]);
        record.user_agent = fieldSetFlags()[6] ? this.user_agent : (CharSequence) defaultValue(fields()[6]);
        record.did = fieldSetFlags()[7] ? this.did : (CharSequence) defaultValue(fields()[7]);
        record.author_uid = fieldSetFlags()[8] ? this.author_uid : (CharSequence) defaultValue(fields()[8]);
        record.day = fieldSetFlags()[9] ? this.day : (Integer) defaultValue(fields()[9]);
        record.source = fieldSetFlags()[10] ? this.source : (CharSequence) defaultValue(fields()[10]);
        record.beat_hostname = fieldSetFlags()[11] ? this.beat_hostname : (CharSequence) defaultValue(fields()[11]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<ActionLog>
    WRITER$ = (org.apache.avro.io.DatumWriter<ActionLog>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<ActionLog>
    READER$ = (org.apache.avro.io.DatumReader<ActionLog>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}