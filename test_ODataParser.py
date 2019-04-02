from unittest import TestCase

from curia_vista_import import ODataParser, schema_to_sql
import xml.etree.ElementTree as ET


class TestODataParser(TestCase):
    XML_SCHEMA_PRE = """<?xml version="1.0" encoding="utf-8"?>
        <edmx:Edmx xmlns:edmx="http://schemas.microsoft.com/ado/2007/06/edmx" Version="1.0">
          <edmx:DataServices xmlns:m="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata" m:DataServiceVersion="1.0" m:MaxDataServiceVersion="2.0">
            <Schema xmlns="http://schemas.microsoft.com/ado/2009/11/edm" Namespace="itsystems.Pd.DataServices.DataModel">
        """
    XML_SCHEMA_POST = """
            </Schema>
          </edmx:DataServices>
        </edmx:Edmx>
        """

    def test_to_schema_real_world_member_party(self):
        xml = TestODataParser.XML_SCHEMA_PRE + """
              <EntityType Name="MemberParty">
                <Key>
                  <PropertyRef Name="ID"/>
                  <PropertyRef Name="Language"/>
                </Key>
                <Property Name="ID" Type="Edm.Int32" Nullable="false"/>
                <Property Name="Language" Type="Edm.String" Nullable="false" MaxLength="2" FixedLength="true" Unicode="false"/>
                <Property Name="PartyNumber" Type="Edm.Int32" Nullable="false"/>
                <Property Name="PartyName" Type="Edm.String" MaxLength="Max" FixedLength="false" Unicode="true"/>
                <Property Name="PersonNumber" Type="Edm.Int32" Nullable="false"/>
                <Property Name="PersonIdCode" Type="Edm.Int32" Nullable="true"/>
                <Property Name="FirstName" Type="Edm.String" MaxLength="40" FixedLength="false" Unicode="true"/>
                <Property Name="LastName" Type="Edm.String" MaxLength="60" FixedLength="false" Unicode="true"/>
                <Property Name="GenderAsString" Type="Edm.String" Nullable="false" MaxLength="1" FixedLength="false" Unicode="false"/>
                <Property Name="PartyFunction" Type="Edm.String" MaxLength="80" FixedLength="false" Unicode="true"/>
                <Property Name="Modified" Type="Edm.DateTime" Precision="3"/>
                <Property Name="PartyAbbreviation" Type="Edm.String" MaxLength="Max" FixedLength="false" Unicode="true"/>
                <NavigationProperty Name="Parties" Relationship="itsystems.Pd.DataServices.DataModel.PartyMemberParty" ToRole="Party" FromRole="MemberParty"/>
                <NavigationProperty Name="MembersCouncil" Relationship="itsystems.Pd.DataServices.DataModel.MemberCouncilMemberParty" ToRole="MemberCouncil" FromRole="MemberParty"/>
              </EntityType>
        """ + TestODataParser.XML_SCHEMA_POST

        self.assertEqual(
            "CREATE TABLE member_party (\n"
            "  id integer NOT NULL,\n"
            "  language char(2) NOT NULL,\n"
            "  party_number integer NOT NULL,\n"
            "  party_name TEXT,\n"
            "  person_number integer NOT NULL,\n"
            "  person_id_code integer,\n"
            "  first_name varchar(40),\n"
            "  last_name varchar(60),\n"
            "  gender_as_string varchar(1) NOT NULL,\n"
            "  party_function varchar(80),\n"
            "  modified timestamp,\n"
            "  party_abbreviation TEXT,\n"
            "  PRIMARY KEY (id, language)\n"
            ");",
            schema_to_sql(xml))

    def test_to_schema_replace_keywords(self):
        xml = TestODataParser.XML_SCHEMA_PRE + """
              <EntityType Name="Keywords">
                <Key>
                  <PropertyRef Name="ID"/>
                </Key>
                <Property Name="ID" Type="Edm.Guid"/>
                <Property Name="Start" Type="Edm.DateTime"/>
                <Property Name="End" Type="Edm.DateTime"/>
                <Property Name="PrefixedStart" Type="Edm.DateTime"/>
              </EntityType>
        """ + TestODataParser.XML_SCHEMA_POST

        self.assertEqual(
            "CREATE TABLE keywords (\n"
            "  id uuid,\n"
            "  start_ timestamp,\n"
            "  end_ timestamp,\n"
            "  prefixed_start timestamp,\n"
            "  PRIMARY KEY (id)\n"
            ");",
            schema_to_sql(xml))

    def test_to_schema_cornercases(self):
        xml = TestODataParser.XML_SCHEMA_PRE + """
              <EntityType Name="Keywords">
                <Key>
                  <PropertyRef Name="ID"/>
                </Key>
                <Property Name="ID" Type="Edm.Guid"/>
                <Property Name="Boolean" Type="Edm.Boolean"/>
                <Property Name="Int16" Type="Edm.Int16"/>
                <Property Name="Int64" Type="Edm.Int64"/>
                <Property Name="Interval" Type="Edm.DateTimeOffset"/>
              </EntityType>
        """ + TestODataParser.XML_SCHEMA_POST

        self.assertEqual(
            "CREATE TABLE keywords (\n"
            "  id uuid,\n"
            "  boolean boolean,\n"
            "  int16 smallint,\n"
            "  int64 bigint,\n"
            "  interval interval,\n"
            "  PRIMARY KEY (id)\n"
            ");",
            schema_to_sql(xml))
