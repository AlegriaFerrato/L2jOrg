module org.l2j.commons {
    requires java.sql;
    requires java.naming;
    requires java.desktop;
    requires org.slf4j;
    requires com.zaxxer.hikari;
    requires spring.data.commons;
    requires spring.data.jdbc;
    requires spring.context;
    requires spring.jdbc;
    requires java.xml.bind;
    requires java.management;
    requires commons.pool;
    requires java.compiler;
    requires commons.math3;
    requires commons.io;
    requires dom4j;
    requires commons.dbcp;
    requires ecj;

    exports org.l2j.commons.util;
    exports org.l2j.commons.xml;
    exports org.l2j.commons.crypt;
    exports org.l2j.commons.status;
    exports org.l2j.commons.lib;
    exports org.l2j.commons;
    exports org.l2j.commons.database;
    exports org.l2j.commons.database.model;
    exports org.l2j.commons.database.annotation;
    exports org.l2j.commons.configuration;
    exports org.l2j.commons.settings;
    exports org.l2j.commons.geometry;
    exports org.l2j.commons.collections;
    exports org.l2j.commons.lang;
    exports org.l2j.commons.listener;
    exports org.l2j.commons.threading;
    exports org.l2j.commons.net.nio.impl;
    exports org.l2j.commons.dbutils;
    exports org.l2j.commons.lang.reference;
    exports org.l2j.commons.util.concurrent.atomic;
    exports org.l2j.commons.dao;
    exports org.l2j.commons.math;
    exports org.l2j.commons.time.cron;
    exports org.l2j.commons.string;
    exports org.l2j.commons.math.random;
    exports org.l2j.commons.logging;
    exports org.l2j.commons.compiler;
    exports org.l2j.commons.annotations;
    exports org.l2j.commons.map.hash;
    exports org.l2j.commons.data.xml;
    exports org.l2j.commons.versioning;
    exports org.l2j.commons.net.nio;
    exports org.l2j.commons.text;
    exports org.l2j.commons.formats.dds;
    exports org.l2j.commons.dbcp;

}