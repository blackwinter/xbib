package org.xbib.util;

import org.xbib.iri.IRI;

public interface ArticleVocabulary {
    
    IRI FABIO_ARTICLE = IRI.create("fabio:Article");

    IRI FABIO_JOURNAL = IRI.create("fabio:Journal");

    IRI FABIO_REVIEW = IRI.create("fabio:Review");

    IRI FABIO_PERIODICAL_VOLUME = IRI.create("fabio:PeriodicalVolume");

    IRI FABIO_PERIODICAL_ISSUE = IRI.create("fabio:PeriodicalIssue");

    IRI FABIO_PRINT_OBJECT = IRI.create("fabio:PrintObject");

    IRI FABIO_HAS_SHORT_TITLE = IRI.create("fabio:hasShortTitle");

    IRI FABIO_HAS_PUBMEDID = IRI.create("fabio:hasPubMedId");

    IRI FABIO_HAS_PUBMEDCENTRALID = IRI.create("fabio:hasPubMedCentralId");

    IRI FABIO_HAS_SUBJECT_TERM = IRI.create("fabio:hasSubjectTerm");

    IRI FABIO_ABSTRACT = IRI.create("fabio:Abstract");

    IRI FABIO_SUBJECT_TERM = IRI.create("fabio:SubjectTerm");

    IRI FRBR_PART = IRI.create("frbr:part");

    IRI FRBR_PARTOF = IRI.create("frbr:partOf");

    IRI FRBR_EMBODIMENT = IRI.create("frbr:embodiment");

    IRI DC_CREATOR = IRI.create("dc:creator");

    IRI DC_CONTRIBUTOR = IRI.create("dc:contributor");

    IRI DC_TITLE = IRI.create("dc:title");

    IRI DC_IDENTIFIER = IRI.create("dc:identifier");

    IRI DC_DATE = IRI.create("dc:date");

    IRI DC_SOURCE = IRI.create("dc:source");

    IRI DCTERMS_ABSTRACT = IRI.create("dcterms:abstract");

    IRI FOAF_AGENT = IRI.create("foaf:agent");

    IRI FOAF_FAMILYNAME = IRI.create("foaf:familyName");

    IRI FOAF_GIVENNAME = IRI.create("foaf:givenName");

    IRI FOAF_NAME = IRI.create("foaf:name");

    IRI FOAF_MBOX = IRI.create("foaf:mbox");

    IRI PRISM_DOI = IRI.create("prism:doi");

    IRI PRISM_PUBLICATIONDATE = IRI.create("prism:publicationDate");

    IRI PRISM_VOLUME = IRI.create("prism:volume");

    IRI PRISM_NUMBER = IRI.create("prism:number");

    IRI PRISM_STARTING_PAGE = IRI.create("prism:startingPage");

    IRI PRISM_ENDING_PAGE = IRI.create("prism:endingPage");

    IRI PRISM_PAGERANGE = IRI.create("prism:pageRange");

    IRI PRISM_PUBLICATIONNAME = IRI.create("prism:publicationName");

    IRI PRISM_ISSN = IRI.create("prism:issn");

    IRI RDFS_LABEL = IRI.create("rdfs:label");

    IRI XBIB_KEY = IRI.create("xbib:key");

    IRI DC_TYPE = IRI.create("dc:type");

    IRI DC_PUBLISHER = IRI.create("dc:publisher");

    IRI DC_RIGHTS = IRI.create("dc:rights");

    IRI DCTERMS_BIBLIOGRAPHIC_CITATION = IRI.create("dcterms:bibliographicCitation");

    IRI XBIB_DOAJ_ID = IRI.create("xbib:doajid");
}
