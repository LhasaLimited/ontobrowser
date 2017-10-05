-- Copyright 2015 Novartis Institutes for Biomedical Research
-- 
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--     http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

CREATE SEQUENCE PRIMARY_KEY_SEQ 
  MAXVALUE 9223372036854775807
  NOCYCLE;

CREATE TABLE CURATOR
(
    CURATOR_ID          NUMBER(12)      NOT NULL
                                        CONSTRAINT CURATOR_PK
                                        PRIMARY KEY,
    USERNAME            VARCHAR2(32)    NOT NULL
                                        CONSTRAINT CURATOR_USERNAME_UNIQUE
                                        UNIQUE,
    PASSWORD            VARCHAR2(256)   NULL,
    PASSWORD_EXPIRED    NUMBER(1)       DEFAULT 0 NOT NULL
                                        CONSTRAINT CURATOR_PW_EXPIRED_CK
                                        CHECK(PASSWORD_EXPIRED IN (0,1)),
    EMAIL_ADDRESS       VARCHAR2(256)   NULL,
    IS_ACTIVE           NUMBER(1)       DEFAULT 1 NOT NULL
                                        CONSTRAINT CURATOR_IS_ACTIVE_CK
                                        CHECK(IS_ACTIVE IN (0,1)),
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT CURATOR_CREATED_BY_FK
                                        REFERENCES CURATOR,
    MODIFIED_DATE       DATE            NULL,
    MODIFIED_BY         NUMBER(12)      NULL
                                        CONSTRAINT CURATOR_MODIFIED_BY_FK
                                        REFERENCES CURATOR                                        
);

CREATE TABLE CURATOR_APPROVAL_WEIGHT
(
    CURATOR_APPROVAL_ID NUMBER(12)      NOT NULL
                                        CONSTRAINT APPROVAL_WEIGHT_PK
                                        PRIMARY KEY,
    CURATOR_ID          NUMBER(12)      NOT NULL
                                        CONSTRAINT APPROVAL_WEIGHT_CURATOR_FK
                                        REFERENCES CURATOR,
    TABLE_NAME          VARCHAR2(32)    NOT NULL
                                        CONSTRAINT APPROVAL_WEIGHT_TABLE_CK
                                        CHECK(TABLE_NAME IN (
                                            'RELATIONSHIP_TYPE',
                                            'ONTOLOGY',
                                            'TERM',
                                            'TERM_RELATIONSHIP',
                                            'TERM_SYNONYM')),
    APPROVAL_WEIGHT     NUMBER(9,8)     NOT NULL
                                        CONSTRAINT CURATOR_APPROVAL_WEIGHT_CK
                                        CHECK(APPROVAL_WEIGHT BETWEEN 0 AND 1),
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT APPROVAL_WEIGHT_CREATED_BY_FK
                                        REFERENCES CURATOR,
    MODIFIED_DATE       DATE            NULL,
    MODIFIED_BY         NUMBER(12)      NULL
                                        CONSTRAINT APPROVAL_WEIGHT_MODIFIED_BY_FK
                                        REFERENCES CURATOR,
    CONSTRAINT APPROVAL_WEIGHT_TABLE_UNIQUE UNIQUE(CURATOR_ID, TABLE_NAME)
);

CREATE TABLE VERSION
(
    VERSION_ID          NUMBER(12)      NOT NULL
                                        CONSTRAINT VERSION_PK
                                        PRIMARY KEY,
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT VERSION_CREATED_BY_FK
                                        REFERENCES CURATOR,
    PUBLISHED_DATE      DATE            NULL,
    PUBLISHED_BY        NUMBER(12)      NULL
                                        CONSTRAINT VERSION_PUBLISHED_BY_FK
                                        REFERENCES CURATOR
);

CREATE TABLE DATASOURCE
(
    DATASOURCE_ID       NUMBER(12)      NOT NULL
                                        CONSTRAINT DATASOURCE_PK
                                        PRIMARY KEY,
    DATASOURCE_NAME     VARCHAR2(64)    NOT NULL
                                        CONSTRAINT DATASOURCE_NAME_UNIQUE 
                                        UNIQUE,
    DATASOURCE_ACRONYM  VARCHAR2(64)    NOT NULL
                                        CONSTRAINT DATASOURCE_ACRONYM_UNIQUE 
                                        UNIQUE,
    DATASOURCE_URI      VARCHAR2(1024)  NULL,
    IS_INTERNAL         NUMBER(1)       NOT NULL
                                        CONSTRAINT DATASOURCE_IS_INTERNAL_CK
                                        CHECK(IS_INTERNAL IN (0,1)),
    IS_PUBLIC           NUMBER(1)       DEFAULT 0
                                        NOT NULL
                                        CONSTRAINT DATASOURCE_IS_PUBLIC_CK
                                        CHECK(IS_PUBLIC IN (0,1)),
    RELEASE_DATE        DATE            NULL,
    VERSION_NUMBER      VARCHAR2(32)    NULL,
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT DATASOURCE_CREATED_BY_FK
                                        REFERENCES CURATOR,
    MODIFIED_DATE       DATE            NULL,
    MODIFIED_BY         NUMBER(12)      NULL
                                        CONSTRAINT DATASOURCE_MODIFIED_BY_FK
                                        REFERENCES CURATOR
);

CREATE TABLE CTRLD_VOCAB_DOMAIN
(
    CTRLD_VOCAB_DOMAIN_ID NUMBER(12)    NOT NULL
                                        CONSTRAINT VOCAB_DOMAIN_PK
                                        PRIMARY KEY,
    CTRLD_VOCAB_DOMAIN  VARCHAR2(64)    NOT NULL
                                        CONSTRAINT VOCAB_DOMAIN_UNIQUE 
                                        UNIQUE,
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT VOCAB_DOMAIN_CREATED_BY_FK
                                        REFERENCES CURATOR,
    MODIFIED_DATE       DATE            NULL,
    MODIFIED_BY         NUMBER(12)      NULL
                                        CONSTRAINT VOCAB_DOMAIN_MODIFIED_BY_FK
                                        REFERENCES CURATOR
);

CREATE TABLE CTRLD_VOCAB_CONTEXT
(
    CTRLD_VOCAB_CONTEXT_ID NUMBER(12)   NOT NULL
                                        CONSTRAINT VOCAB_CONTEXT_PK
                                        PRIMARY KEY,
    CTRLD_VOCAB_CONTEXT VARCHAR2(64)    NOT NULL
                                        CONSTRAINT VOCAB_CONTEXT_UNIQUE 
                                        UNIQUE,
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT VOCAB_CONTEXT_CREATED_BY_FK
                                        REFERENCES CURATOR,
    MODIFIED_DATE       DATE            NULL,
    MODIFIED_BY         NUMBER(12)      NULL
                                        CONSTRAINT VOCAB_CONTEXT_MODIFIED_BY_FK
                                        REFERENCES CURATOR
);

CREATE TABLE CTRLD_VOCAB
(
    CTRLD_VOCAB_ID      NUMBER(12)      NOT NULL
                                        CONSTRAINT CTRLD_VOCAB_PK
                                        PRIMARY KEY,
    DATASOURCE_ID       NUMBER(12)      NOT NULL
                                        CONSTRAINT CTRLD_VOCAB_DATASOURCE_FK
                                        REFERENCES DATASOURCE,
    CTRLD_VOCAB_NAME    VARCHAR2(64)    NOT NULL,
    CTRLD_VOCAB_DOMAIN_ID NUMBER(12)    NOT NULL
                                        CONSTRAINT CTRLD_VOCAB_DOMAIN_FK
                                        REFERENCES CTRLD_VOCAB_DOMAIN,
    CTRLD_VOCAB_CONTEXT_ID NUMBER(12)   NOT NULL
                                        CONSTRAINT CTRLD_VOCAB_CONTEXT_FK
                                        REFERENCES CTRLD_VOCAB_CONTEXT,
    REFERENCE_ID        VARCHAR2(32)    NULL,
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT CTRLD_VOCAB_CREATED_BY_FK
                                        REFERENCES CURATOR,
    MODIFIED_DATE       DATE            NULL,
    MODIFIED_BY         NUMBER(12)      NULL
                                        CONSTRAINT CTRLD_VOCAB_MODIFIED_BY_FK
                                        REFERENCES CURATOR,
    CONSTRAINT CTRLD_VOCAB_NAME_UNIQUE UNIQUE(CTRLD_VOCAB_NAME, DATASOURCE_ID)
);

CREATE TABLE CTRLD_VOCAB_TERM
(
    CTRLD_VOCAB_TERM_ID NUMBER(12)      NOT NULL
                                        CONSTRAINT CTRLD_VOCAB_TERM_PK
                                        PRIMARY KEY,
    CTRLD_VOCAB_ID      NUMBER(12)      NOT NULL
                                        CONSTRAINT VOCAB_TERM_VOCAB_FK
                                        REFERENCES CTRLD_VOCAB,
    CTRLD_VOCAB_TERM    VARCHAR2(256)   NOT NULL,
    REFERENCE_ID        VARCHAR2(32)    NULL,
    USAGE_COUNT         NUMBER(12)      DEFAULT 1 NOT NULL,
    IS_EXCLUDED         NUMBER(1)       DEFAULT 0 NOT NULL,
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT VOCAB_TERM_CREATED_BY_FK
                                        REFERENCES CURATOR,
    MODIFIED_DATE       DATE            NULL,
    MODIFIED_BY         NUMBER(12)      NULL
                                        CONSTRAINT VOCAB_TERM_MODIFIED_BY_FK
                                        REFERENCES CURATOR
);

CREATE UNIQUE INDEX CTRLD_VOCAB_TERM_IDX
ON CTRLD_VOCAB_TERM(CTRLD_VOCAB_TERM, CTRLD_VOCAB_ID);

CREATE INDEX CTRLD_VOCAB_VOCAB_IDX
ON CTRLD_VOCAB_TERM(CTRLD_VOCAB_ID);

CREATE TABLE CTRLD_VOCAB_TERM_LINK
(
    CTRLD_VOCAB_TERM_LINK_ID NUMBER(12) NOT NULL
                                        CONSTRAINT CTRLD_VOCAB_TERM_LINK_PK
                                        PRIMARY KEY,
    CTRLD_VOCAB_TERM_ID NUMBER(12)      NOT NULL
                                        CONSTRAINT CTRLD_VOCAB_TERM_LINK_FK1
                                        REFERENCES CTRLD_VOCAB_TERM,
    LINKED_CTRLD_VOCAB_TERM_ID NUMBER(12) NOT NULL
                                        CONSTRAINT CTRLD_VOCAB_TERM_LINK_FK2
                                        REFERENCES CTRLD_VOCAB_TERM,
    USAGE_COUNT         NUMBER(12)      DEFAULT 1 NOT NULL,
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT VOCAB_TERM_LINK_CREATED_BY_FK
                                        REFERENCES CURATOR,
    CREATED_LOAD_NUMBER NUMBER(12)      NOT NULL,
    MODIFIED_DATE       DATE            NULL,
    MODIFIED_BY         NUMBER(12)      NULL
                                        CONSTRAINT VOCAB_TERM_LINK_MODIFIED_BY_FK
                                        REFERENCES CURATOR,
    MODIFIED_LOAD_NUMBER NUMBER(12)     NULL
);

CREATE UNIQUE INDEX CTRLD_VOCAB_TERM_LINK_IDX
ON CTRLD_VOCAB_TERM_LINK(CTRLD_VOCAB_TERM_ID, LINKED_CTRLD_VOCAB_TERM_ID);

CREATE TABLE RELATIONSHIP_TYPE (
    RELATIONSHIP_TYPE_ID NUMBER(12)     NOT NULL
                                        CONSTRAINT RELATIONSHIP_TYPE_PK
                                        PRIMARY KEY,
    RELATIONSHIP_TYPE   VARCHAR2(64)    NOT NULL
                                        CONSTRAINT RELATIONSHIP_TYPE_UNIQUE 
                                        UNIQUE,
    NAME                VARCHAR2(256)   NOT NULL,
    DEFINTION           VARCHAR2(256)   NOT NULL,
    IS_CYCLIC           NUMBER(1)       NOT NULL
                                        CONSTRAINT RELSHIP_TYPE_CYCLIC_CK
                                        CHECK(IS_CYCLIC IN (0,1)),
    IS_SYMMETRIC        NUMBER(1)       NOT NULL
                                        CONSTRAINT RELSHIP_TYPE_SYMMETRIC_CK
                                        CHECK(IS_SYMMETRIC IN (0,1)),
    IS_TRANSITIVE       NUMBER(1)       NOT NULL
                                        CONSTRAINT RELSHIP_TYPE_TRANSITIVE_CK
                                        CHECK(IS_TRANSITIVE IN (0,1)),
    INVERSE_OF          NUMBER(12)      NULL
                                        CONSTRAINT RELSHIP_TYPE_INVERSE_FK
                                        REFERENCES RELATIONSHIP_TYPE,
    TRANSITIVE_OVER     NUMBER(12)      NULL
                                        CONSTRAINT RELSHIP_TYPE_TRANSITIVE_FK
                                        REFERENCES RELATIONSHIP_TYPE,
    EDGE_COLOUR         CHAR(7)         NULL,
    STATUS              VARCHAR2(8)     DEFAULT 'PENDING' NOT NULL
                                        CONSTRAINT RELATIONSHIP_STATUS_CK
                                        CHECK(STATUS IN (
                                            'PENDING',
                                            'APPROVED',
                                            'REJECTED',
                                            'OBSOLETE')),
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT RELSHIP_TYPE_CREATED_BY_FK
                                        REFERENCES CURATOR,
    CREATED_VERSION_ID  NUMBER(12)      NOT NULL
                                        CONSTRAINT RSHIP_TYPE_CREATED_VERSION_FK
                                        REFERENCES VERSION,
    APPROVED_VERSION_ID NUMBER(12)      NULL
                                        CONSTRAINT RSHIP_TYPE_APPROVED_VERSION_FK
                                        REFERENCES VERSION,
    OBSOLETE_VERSION_ID NUMBER(12)      NULL
                                        CONSTRAINT RSHIP_TYPE_OBSOLETE_VERSION_FK
                                        REFERENCES VERSION,
    REPLACED_BY         NUMBER(12)      NULL
                                        CONSTRAINT RSHIP_TYPE_REPLACED_BY_FK
                                        REFERENCES RELATIONSHIP_TYPE
);

CREATE TABLE ONTOLOGY
(
    ONTOLOGY_ID         NUMBER(12)      NOT NULL
                                        CONSTRAINT ONTOLOGY_PK
                                        PRIMARY KEY,
    ONTOLOGY_NAME       VARCHAR2(64)    NOT NULL
                                        CONSTRAINT ONTOLOGY_NAME_UNIQUE 
                                        UNIQUE,
    DESCRIPTION         VARCHAR2(1024)  NULL,
    IS_INTERNAL         NUMBER(1)       NOT NULL
                                        CONSTRAINT ONTOLOGY_IS_INTERNAL_CK
                                        CHECK(IS_INTERNAL IN (0,1)),
    SOURCE_NAMESPACE    VARCHAR2(256)   NULL,
    SOURCE_URI          VARCHAR2(1024)  NULL,
    SOURCE_RELEASE      VARCHAR2(1024)   NULL,
    SOURCE_DATE         DATE            NULL,
    SOURCE_FORMAT       VARCHAR2(32)    NULL,
    REFERENCE_ID_PREFIX VARCHAR2(16)    NOT NULL
                                        CONSTRAINT ONTOLOGY_REF_ID_PREFIX_UNIQUE 
                                        UNIQUE,
    REFERENCE_ID_VALUE  NUMBER(7)       DEFAULT 0 NOT NULL,
    IS_CODELIST         NUMBER(1)       DEFAULT 0 NOT NULL
                                        CONSTRAINT ONTOLOGY_IS_CODELIST_CK
                                        CHECK(IS_CODELIST IN (0,1)),
    REFERENCE_ID        VARCHAR2(32)    NULL,
    STATUS              VARCHAR2(8)     DEFAULT 'PENDING' NOT NULL
                                        CONSTRAINT ONTOLOGY_STATUS_CK
                                        CHECK(STATUS IN (
                                            'PENDING',
                                            'APPROVED',
                                            'REJECTED',
                                            'OBSOLETE')),
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT ONTOLOGY_CREATED_BY_FK
                                        REFERENCES CURATOR,
    CREATED_VERSION_ID  NUMBER(12)      NOT NULL
                                        CONSTRAINT ONTOLOGY_CREATED_VERSION_FK
                                        REFERENCES VERSION,
    APPROVED_VERSION_ID NUMBER(12)      NULL
                                        CONSTRAINT ONTOLOGY_APPROVED_VERSION_FK
                                        REFERENCES VERSION,
    OBSOLETE_VERSION_ID NUMBER(12)      NULL
                                        CONSTRAINT ONTOLOGY_OBSOLETE_VERSION_FK
                                        REFERENCES VERSION,
    REPLACED_BY         NUMBER(12)      NULL
                                        CONSTRAINT ONTOLOGY_REPLACED_BY_FK
                                        REFERENCES ONTOLOGY
);

CREATE UNIQUE INDEX ONTOLOGY_REFERENCE_ID_IDX
ON ONTOLOGY(REFERENCE_ID);

CREATE TABLE CTRLD_VOCAB_DOMAIN_ONTOLOGY
(
    CTRLD_VOCAB_DOMAIN_ONTOLOGY_ID      NUMBER(12) NOT NULL
                                        CONSTRAINT CTRLD_VOCAB_DOMAIN_ONTOLOGY_PK
                                        PRIMARY KEY,
    CTRLD_VOCAB_DOMAIN_ID               NUMBER(12) NOT NULL
                                        CONSTRAINT VOCAB_DOMAIN_ONTO_DOMAIN_FK
                                        REFERENCES CTRLD_VOCAB_DOMAIN,
    ONTOLOGY_ID         NUMBER(12)      NOT NULL
                                        CONSTRAINT VOCAB_DOMAIN_ONTO_ONTO_FK
                                        REFERENCES ONTOLOGY,
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT VOCAB_DOMAIN_ONTO_CREATED_FK
                                        REFERENCES CURATOR,
    MODIFIED_DATE       DATE            NULL,
    MODIFIED_BY         NUMBER(12)      NULL
                                        CONSTRAINT VOCAB_DOMAIN_ONTO_MODIFIED_FK
                                        REFERENCES CURATOR
);

CREATE UNIQUE INDEX VOCAB_DOMAIN_ONTOLOGY_IDX
ON CTRLD_VOCAB_DOMAIN_ONTOLOGY(CTRLD_VOCAB_DOMAIN_ID, ONTOLOGY_ID);

CREATE TABLE TERM (
    TERM_ID             NUMBER(12)      NOT NULL
                                        CONSTRAINT TERM_PK
                                        PRIMARY KEY,
    ONTOLOGY_ID         NUMBER(12)      NOT NULL
                                        CONSTRAINT TERM_ONTOLOGY_FK
                                        REFERENCES ONTOLOGY,
    TERM_NAME           VARCHAR2(256)   NOT NULL,
    REFERENCE_ID        VARCHAR2(32)    NOT NULL,
    DEFINITION          VARCHAR2(4000)  NULL,
    DEFINITION_URL      VARCHAR2(1024)  NULL,
    COMMENTS            VARCHAR2(4000)  NULL,
    IS_ROOT             NUMBER(1)       DEFAULT 0 NOT NULL
                                        CONSTRAINT TERM_IS_ROOT_CK
                                        CHECK(IS_ROOT IN (0,1)),
    STATUS              VARCHAR2(8)     DEFAULT 'PENDING' NOT NULL
                                        CONSTRAINT TERM_STATUS_CK
                                        CHECK(STATUS IN (
                                            'PENDING',
                                            'APPROVED',
                                            'REJECTED',
                                            'OBSOLETE')),
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT TERM_CREATED_BY_FK
                                        REFERENCES CURATOR,
    CREATED_VERSION_ID  NUMBER(12)      NOT NULL
                                        CONSTRAINT TERM_CREATED_VERSION_FK
                                        REFERENCES VERSION,
    APPROVED_VERSION_ID NUMBER(12)      NULL
                                        CONSTRAINT TERM_APPROVED_VERSION_FK
                                        REFERENCES VERSION,
    OBSOLETE_VERSION_ID NUMBER(12)      NULL
                                        CONSTRAINT TERM_OBSOLETE_VERSION_FK
                                        REFERENCES VERSION,
    REPLACED_BY         NUMBER(12)      NULL
                                        CONSTRAINT TERM_REPLACED_BY_FK
                                        REFERENCES TERM
);

CREATE UNIQUE INDEX TERM_ONTOLOGY_IDX
ON TERM(LOWER(TERM_NAME), ONTOLOGY_ID);

CREATE UNIQUE INDEX TERM_REFERENCE_ID_IDX
ON TERM(UPPER(REFERENCE_ID));

CREATE BITMAP INDEX TERM_IS_ROOT_IDX
ON TERM(IS_ROOT);

CREATE TABLE TERM_XREF
(
    TERM_XREF_ID NUMBER(12)             NOT NULL
                                        CONSTRAINT TERM_XREF_PK
                                        PRIMARY KEY,
    TERM_ID            NUMBER(12)       NOT NULL
                                        CONSTRAINT TERM_XREF_TERM_FK
                                        REFERENCES TERM,
    DATASOURCE_ID      NUMBER(12)       NULL
                                        CONSTRAINT TERM_XREF_DATASOURCE_FK
                                        REFERENCES DATASOURCE,
    REFERENCE_ID       VARCHAR2(256)    NULL,
    DESCRIPTION        VARCHAR2(1024)   NULL,
    XREF_URL           VARCHAR2(1024)   NULL,    
    IS_DEFINITION_XREF NUMBER(1)        DEFAULT 0 NOT NULL
                                        CONSTRAINT TERM_XREF_IS_DEF_CK
                                        CHECK(IS_DEFINITION_XREF IN (0,1)),
    STATUS              VARCHAR2(8)     DEFAULT 'PENDING' NOT NULL
                                        CONSTRAINT TERM_XREF_STATUS_CK
                                        CHECK(STATUS IN (
                                            'PENDING',
                                            'APPROVED',
                                            'REJECTED',
                                            'OBSOLETE')),
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT TERM_XREF_CREATED_BY_FK
                                        REFERENCES CURATOR,
    CREATED_VERSION_ID  NUMBER(12)      NULL
                                        CONSTRAINT TERM_XREF_CREATED_VERSION_FK
                                        REFERENCES VERSION,
    APPROVED_VERSION_ID NUMBER(12)      NULL
                                        CONSTRAINT TERM_XREF_APPROVED_VERSION_FK
                                        REFERENCES VERSION,
    OBSOLETE_VERSION_ID NUMBER(12)      NULL
                                        CONSTRAINT TERM_XREF_OBSOLETE_VERSION_FK
                                        REFERENCES VERSION,
    REPLACED_BY         NUMBER(12)      NULL
                                        CONSTRAINT TERM_XREF_REPLACED_BY_FK
                                        REFERENCES TERM_XREF,
	MODIFIED_DATE       DATE            NULL,
    MODIFIED_BY         NUMBER(12)      NULL
                                        CONSTRAINT TERM_XREF_MODIFIED_BY_FK
                                        REFERENCES CURATOR
);

CREATE INDEX TERM_XREF_TERM_IDX
ON TERM_XREF(TERM_ID);

CREATE TABLE TERM_RELATIONSHIP (
    TERM_RELATIONSHIP_ID NUMBER(12)     NOT NULL
                                        CONSTRAINT TERM_RELSHIP_PK
                                        PRIMARY KEY,
    TERM_ID             NUMBER(12)      NOT NULL
                                        CONSTRAINT TERM_RELSHIP_TERM_FK
                                        REFERENCES TERM,
    RELATED_TERM_ID     NUMBER(12)      NOT NULL
                                        CONSTRAINT TERM_RELSHIP_RELATED_TERM_FK
                                        REFERENCES TERM,
    RELATIONSHIP_TYPE_ID NUMBER(12)     NOT NULL
                                        CONSTRAINT TERM_RELSHIP_TYPE_FK
                                        REFERENCES RELATIONSHIP_TYPE,
    IS_INTERSECTION     NUMBER(1)       DEFAULT 0 NOT NULL,
    STATUS              VARCHAR2(8)     DEFAULT 'PENDING' NOT NULL
                                        CONSTRAINT TERM_RELSHIP_STATUS_CK
                                        CHECK(STATUS IN (
                                            'PENDING',
                                            'APPROVED',
                                            'REJECTED',
                                            'OBSOLETE')),
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT TERM_RSHIP_CREATED_BY_FK
                                        REFERENCES CURATOR,
    CREATED_VERSION_ID  NUMBER(12)      NOT NULL
                                        CONSTRAINT TERM_RSHIP_CREATED_VERSION_FK
                                        REFERENCES VERSION,
    APPROVED_VERSION_ID NUMBER(12)      NULL
                                        CONSTRAINT TERM_RSHIP_APRROVED_VERSION_FK
                                        REFERENCES VERSION,
    OBSOLETE_VERSION_ID NUMBER(12)      NULL
                                        CONSTRAINT TERM_RSHIP_OBSOLETE_VERSION_FK
                                        REFERENCES VERSION,
    REPLACED_BY         NUMBER(12)      NULL
                                        CONSTRAINT TERM_RSHIP_REPLACED_BY_FK
                                        REFERENCES TERM_RELATIONSHIP
);

CREATE UNIQUE INDEX TERM_RELSHIP_IDX
ON TERM_RELATIONSHIP(TERM_ID, RELATED_TERM_ID, RELATIONSHIP_TYPE_ID);

CREATE INDEX TERM_RELSHIP_CHILD_IDX
ON TERM_RELATIONSHIP(RELATED_TERM_ID);

CREATE TABLE TERM_SYNONYM (
    TERM_SYNONYM_ID     NUMBER(12)      NOT NULL
                                        CONSTRAINT TERM_SYNONYM_PK
                                        PRIMARY KEY,
    TERM_ID             NUMBER(12)      NOT NULL
                                        CONSTRAINT TERM_SYNONYM_TERM_FK
                                        REFERENCES TERM,                           
    TERM_SYNONYM        VARCHAR2(256)   NOT NULL,
    SYNONYM_TYPE        VARCHAR2(8)     NOT NULL
                                        CONSTRAINT TERM_SYNONYM_TYPE_CK
                                        CHECK(SYNONYM_TYPE IN (
                                            'BROAD',
                                            'EXACT',
                                            'NARROW',
                                            'RELATED')),
    CTRLD_VOCAB_TERM_ID NUMBER(12)      NULL
                                        CONSTRAINT TERM_SYNONYM_VOCAB_TERM_FK
                                        REFERENCES CTRLD_VOCAB_TERM,
    DATASOURCE_ID       NUMBER(12)      NULL
                                        CONSTRAINT TERM_SYNONYM_DATASOURCE_FK
                                        REFERENCES DATASOURCE,
    REFERENCE_ID        VARCHAR2(256)   NULL,
    XREF_URL            VARCHAR2(1024)  NULL,
    DESCRIPTION         VARCHAR2(1024)  NULL,
    STATUS              VARCHAR2(8)     DEFAULT 'PENDING' NOT NULL
                                        CONSTRAINT TERM_SYNONYM_STATUS_CK
                                        CHECK(STATUS IN (
                                            'PENDING',
                                            'APPROVED',
                                            'REJECTED',
                                            'OBSOLETE')),
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT TERM_SYNON_CREATED_BY_FK
                                        REFERENCES CURATOR,
    CREATED_VERSION_ID  NUMBER(12)      NOT NULL
                                        CONSTRAINT TERM_SYNON_CREATED_VERSION_FK
                                        REFERENCES VERSION,
    APPROVED_VERSION_ID NUMBER(12)      NULL
                                        CONSTRAINT TERM_SYNON_APPROVED_VERSION_FK
                                        REFERENCES VERSION,
    OBSOLETE_VERSION_ID NUMBER(12)      NULL
                                        CONSTRAINT TERM_SYNON_OBSOLETE_VERSION_FK
                                        REFERENCES VERSION,
    REPLACED_BY         NUMBER(12)      NULL
                                        CONSTRAINT TERM_SYNON_REPLACED_BY_FK
                                        REFERENCES TERM_SYNONYM
);

CREATE INDEX TERM_SYNONYM_TERM_IDX
ON TERM_SYNONYM(TERM_ID);

CREATE INDEX TERM_SYNONYM_SYNONYM_IDX
ON TERM_SYNONYM(LOWER(TRIM(TERM_SYNONYM)));

CREATE UNIQUE INDEX TERM_SYNONYM_VOCAB_TERM_IDX
ON TERM_SYNONYM(CTRLD_VOCAB_TERM_ID, NVL2(CTRLD_VOCAB_TERM_ID,
  CASE STATUS
    WHEN 'REJECTED' THEN TERM_ID
    WHEN 'OBSOLETE' THEN TERM_ID
    ELSE 0
  END, NULL));

CREATE TABLE CURATOR_ACTION
(
    CURATOR_ACTION_ID   NUMBER(12)      NOT NULL
                                        CONSTRAINT CURATOR_ACTION_PK
                                        PRIMARY KEY,
    CURATOR_ID          NUMBER(12)      NOT NULL
                                        CONSTRAINT CURATOR_ACTION_CURATOR_FK
                                        REFERENCES CURATOR,
    ACTION              VARCHAR2(8)     NOT NULL
                                        CONSTRAINT CURATOR_ACTION_ACTION_CK
                                        CHECK(ACTION IN (
                                            'APPROVE',
                                            'REJECT',
                                            'REPLACE')),
    COMMENTS           VARCHAR2(1024)   NULL,
    ACTION_DATE        DATE             DEFAULT SYSDATE NOT NULL,
    RELATIONSHIP_TYPE_ID NUMBER(12)     NULL
                                        CONSTRAINT CURATOR_ACTION_RELSHIP_TYPE_FK
                                        REFERENCES RELATIONSHIP_TYPE,
    ONTOLOGY_ID        NUMBER(12)       NULL
                                        CONSTRAINT CURATOR_ACTION_ONTOLOGY_FK
                                        REFERENCES ONTOLOGY,
    TERM_ID            NUMBER(12)       NULL
                                        CONSTRAINT CURATOR_ACTION_TERM_FK
                                        REFERENCES TERM,
    TERM_XREF_ID       NUMBER(12)       NULL
                                        CONSTRAINT CURATOR_ACTION_TERM_XREF_FK
                                        REFERENCES TERM_XREF,
    TERM_RELATIONSHIP_ID NUMBER(12)     NULL
                                        CONSTRAINT CURATOR_ACTION_TERM_RELSHIP_FK
                                        REFERENCES TERM_RELATIONSHIP,
    TERM_SYNONYM_ID    NUMBER(12)       NULL
                                        CONSTRAINT CURATOR_ACTION_TERM_SYNON_FK
                                        REFERENCES TERM_SYNONYM
);

-- DROP TABLE ANNOTATION_TYPE;
CREATE TABLE ANNOTATION_TYPE (
    ANNOTATION_TYPE_ID NUMBER(12)     NOT NULL
                                        CONSTRAINT ANNT_TYPE_PK
                                        PRIMARY KEY,
    ANNOTATION_TYPE   VARCHAR2(128)    NOT NULL,
    PREFIXED_XML_TYPE   VARCHAR2(64)   NOT NULL,
    STATUS              VARCHAR2(8)     DEFAULT 'PENDING' NOT NULL
                                        CONSTRAINT ANNT_TYPE_STATUS_CK
                                        CHECK(STATUS IN (
                                            'PENDING',
                                            'APPROVED',
                                            'REJECTED',
                                            'OBSOLETE')),
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT ANNT_TYPE_CREATED_BY_FK
                                        REFERENCES CURATOR,
    CREATED_VERSION_ID  NUMBER(12)      NOT NULL
                                        CONSTRAINT ANNT_TYPE_CREATED_VERSION_FK
                                        REFERENCES VERSION,
    APPROVED_VERSION_ID NUMBER(12)      NULL
                                        CONSTRAINT ANNT_TYPE_APPROVED_VERSION_FK
                                        REFERENCES VERSION,
    OBSOLETE_VERSION_ID NUMBER(12)      NULL
                                        CONSTRAINT ANNT_TYPE_OBSOLETE_VERSION_FK
                                        REFERENCES VERSION,
    REPLACED_BY         NUMBER(12)      NULL
                                        CONSTRAINT ANNT_TYPE_REPLACED_BY_FK
                                        REFERENCES ANNOTATION_TYPE
);

-- ALTER TABLE CURATOR_ACTION DROP COLUMN ANNOTATION_TYPE_ID;
ALTER TABLE CURATOR_ACTION ADD ANNOTATION_TYPE_ID NUMBER(12) NULL
                                        CONSTRAINT CURATOR_ACTION_ATTN_TYPE_FK 
                                        REFERENCES ANNOTATION_TYPE;

CREATE TABLE ANNOTATION (
    ANNOTATION_ID NUMBER(12)     NOT NULL
                                        CONSTRAINT ANNT_PK
                                        PRIMARY KEY,
    ANNOTATION_TYPE_ID NUMBER(12) NOT NULL
                                        CONSTRAINT ANNT_TYPE_FK
   
    TERM_ID            NUMBER(12)       NULL
                                        CONSTRAINT ANNT_TERM_FK
                                    
    ANNOTATION        VARCHAR2(4000)   NOT NULL,
    STATUS              VARCHAR2(8)     DEFAULT 'PENDING' NOT NULL
                                        CONSTRAINT ANNT_STATUS_CK
                                        CHECK(STATUS IN (
                                            'PENDING',
                                            'APPROVED',
                                            'REJECTED',
                                            'OBSOLETE')),
    CREATED_DATE        DATE            DEFAULT SYSDATE NOT NULL,
    CREATED_BY          NUMBER(12)      NOT NULL
                                        CONSTRAINT ANNT_CREATED_BY_FK
                                        REFERENCES CURATOR,
    CREATED_VERSION_ID  NUMBER(12)      NOT NULL
                                        CONSTRAINT ANNT_CREATED_VERSION_FK
                                        REFERENCES VERSION,
    APPROVED_VERSION_ID NUMBER(12)      NULL
                                        CONSTRAINT ANNT_APPROVED_VERSION_FK
                                        REFERENCES VERSION,
    OBSOLETE_VERSION_ID NUMBER(12)      NULL
                                        CONSTRAINT ANNT_OBSOLETE_VERSION_FK
                                        REFERENCES VERSION,
    REPLACED_BY         NUMBER(12)      NULL
                                        CONSTRAINT ANNT_REPLACED_BY_FK
                                        REFERENCES ANNOTATION
);

ALTER TABLE CURATOR_ACTION ADD ANNOTATION_ID NUMBER(12) NULL
                                        CONSTRAINT CURATOR_ACTION_ATTN_FK
                                        REFERENCES ANNOTATION;

ALTER TABLE CURATOR_APPROVAL_WEIGHT DROP CONSTRAINT APPROVAL_WEIGHT_TABLE_CK;

ALTER TABLE CURATOR_APPROVAL_WEIGHT 
ADD CONSTRAINT APPROVAL_WEIGHT_TABLE_CK CHECK(TABLE_NAME IN (
                                            'RELATIONSHIP_TYPE',
                                            'ONTOLOGY',
                                            'TERM',
                                            'TERM_RELATIONSHIP',
                                            'TERM_SYNONYM',
                                            'ANNOTATION_TYPE',
                                            'ANNOTATION'));
                                            
-- 20170822                                            
ALTER TABLE TERM_RELATIONSHIP ADD ONTOLOGY_ID NUMBER(12) NULL
                                        CONSTRAINT TERM_RELSHIP_ONTOLOGY_FK
                                        REFERENCES ONTOLOGY;
                                        
ALTER TABLE ANNOTATION ADD ONTOLOGY_ID NUMBER(12) NULL
                                        CONSTRAINT ANNOTATION_ONTOLOGY_FK
                                        REFERENCES ONTOLOGY;
                                        
ALTER TABLE ANNOTATION_TYPE ADD ONTOLOGY_ID NUMBER(12) NULL
                                        CONSTRAINT ANNT_TYPE_ONTOLOGY_FK
                                        REFERENCES ONTOLOGY;

ALTER TABLE RELATIONSHIP_TYPE ADD ONTOLOGY_ID NUMBER(12) NULL
                                        CONSTRAINT REL_TYPE_ONTOLOGY_FK
                                        REFERENCES ONTOLOGY;
                                        
-- 20170823

ALTER TABLE ONTOLOGY MODIFY REFERENCE_ID_PREFIX NULL;
ALTER TABLE ONTOLOGY MODIFY REFERENCE_ID_VALUE NULL;
ALTER TABLE ONTOLOGY MODIFY ONTOLOGY_NAME VARCHAR(128);
ALTER TABLE ONTOLOGY DISABLE CONSTRAINT ONTOLOGY_REF_ID_PREFIX_UNIQUE;
                                                                           
CREATE TABLE ONTOLOGY_IMPORTED (
  ONTOLOGY_ID NUMBER(12) NOT NULL CONSTRAINT ONT_IMP_PARENT_FK REFERENCES ONTOLOGY(ONTOLOGY_ID),
  IMPORTED_ONTOLOGY_ID NUMBER(12) NOT NULL CONSTRAINT ONT_IMP_CHILD_FK REFERENCES ONTOLOGY(ONTOLOGY_ID)
);                                                                    

ALTER TABLE ANNOTATION_TYPE DROP CONSTRAINT ANNT_TYPE_UNIQUE;
           
ALTER TABLE ANNOTATION DROP CONSTRAINT SYS_C007941;                          

ALTER TABLE ANNOTATION_TYPE ADD DEFINITION_URL VARCHAR2(1024) NULL;

ALTER TABLE TERM ADD TERM_TYPE CHAR(1) DEFAULT 'C' NOT NULL  
CONSTRAINT TERM_TYPE_CK CHECK(TERM_TYPE IN ('C', 'I')) ;

-- make reference longer and recreate the index ( with ontology id )
DROP INDEX TERM_REFERENCE_ID_IDX;

ALTER TABLE TERM MODIFY REFERENCE_ID VARCHAR(64);
CREATE UNIQUE INDEX TERM_REFERENCE_ID_IDX
ON TERM(ONTOLOGY_ID, UPPER(REFERENCE_ID));

CREATE TABLE ONTOLOGY_ALIAS (
    ID NUMBER(12) NOT NULL CONSTRAINT ONT_ALS_PK PRIMARY KEY,
    ONTOLOGY_ID NUMBER(12) NOT NULL CONSTRAINT ONT_ALS_PARENT_FK REFERENCES ONTOLOGY(ONTOLOGY_ID),
    ALIAS_URI VARCHAR2(1024) NOT NULL
);

DROP INDEX TERM_ONTOLOGY_IDX;
ALTER TABLE TERM MODIFY TERM_NAME VARCHAR2(1024);
CREATE UNIQUE INDEX TERM_ONTOLOGY_IDX ON TERM(TERM_NAME, ONTOLOGY_ID);

ALTER TABLE ONTOLOGY ADD IS_INTERMEDIATE  NUMBER(1)       DEFAULT 0 NOT NULL
                                        CONSTRAINT ONTOLOGY_IS_INTERMEDIATE_CK
                                        CHECK(IS_INTERMEDIATE IN (0,1));

ALTER SEQUENCE primary_key_seq INCREMENT BY 20;

ALTER TABLE relationship_type ADD definition_url VARCHAR2(1024) NULL;


-- for graph queries

create index TERM_RELSHIP_TYPE_STATUS_IDX on TERM_RELATIONSHIP("RELATIONSHIP_TYPE_ID","STATUS");
create index TERM_ID_STATUS_ONTOLOGY_IDX on ARTUR.TERM("STATUS","TERM_ID","ONTOLOGY_ID");

ALTER TABLE ANNOTATION_TYPE ADD PROPERTY_TYPE CHAR(1) DEFAULT 'A' NOT NULL  
CONSTRAINT PROPERTY_TYPE_CK CHECK(PROPERTY_TYPE IN ('A', 'D')) ;