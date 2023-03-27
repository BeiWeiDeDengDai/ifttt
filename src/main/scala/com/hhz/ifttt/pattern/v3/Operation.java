package com.hhz.ifttt.pattern.v3;

public interface Operation {
        /**
         * The operation that read the current state of a record, most typically during snapshots.
         */
        String READ = "r";
        /**
         * An operation that resulted in a new record being created in the source.
         */
        String CREATE = "c";
        /**
         * An operation that resulted in an existing record being updated in the source.
         */
        String UPDATE = "u";
        /**
         * An operation that resulted in an existing record being removed from or deleted in the source.
         */
        String DELETE = "d";
        /**
         * An operation that resulted in an existing table being truncated in the source.
         */


    }