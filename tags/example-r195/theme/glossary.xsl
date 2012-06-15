<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE xsl:stylesheet[
<!ENTITY catts "@id|@class|@title|@xml:lang">
<!ENTITY cncatts "@id|@title|@xml:lang">
 ]>
<xsl:stylesheet
    version="1.0"
    xmlns:out="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:m="http://www.w3.org/1998/Math/MathML"
    xmlns:wicket="http://wicket.apache.org"
    xmlns="http://www.w3.org/1999/xhtml" 
    exclude-result-prefixes="dtb">
    
    <xsl:import href="dtbook2xhtml.xsl"/>
    <xsl:import href="common.xsl"/>
    
    <xsl:param name="mini"/>
    
    <!-- glossary-specific -->

    <xsl:template match="dtb:h1">
      <!-- already shown in header -->
    </xsl:template>

    <!-- "minionly" Short Definitions need to be hidden in full glossary -->
    <xsl:template match="dtb:level2[@class='minionly']">
      <xsl:if test="$mini">
        <div>
    	  <xsl:copy-of select="&cncatts;"/>
       	  <xsl:attribute name="class">
            <xsl:value-of select="concat('level2 ', @class)" />
       	  </xsl:attribute>  
       	  <xsl:apply-templates/>
     	</div>
      </xsl:if>
    </xsl:template>

    <xsl:template match="dtb:level2">
	  <!-- If this is a "minialso" section, show in both places.
	       If it's unmarked, show in large glossary, and show in mini glossary if there are no marked sections. -->
      <xsl:if test="not($mini) or @class='minialso' or not(../*[@class='minionly' or @class='minialso'])">
        <div>
    	  <xsl:copy-of select="&cncatts;"/>
       	  <xsl:attribute name="class">
            <xsl:value-of select="concat('level2 ', @class)" />
       	  </xsl:attribute>  
       	  <xsl:apply-templates/>
     	</div>
      </xsl:if>
    </xsl:template>

    <xsl:template match="dtb:h2">
      <h2>
        <xsl:copy-of select="&catts;"/>
        <xsl:apply-templates/>
      </h2>
    </xsl:template>

    <xsl:template match="dtb:p[@title]">
      <p>
        <xsl:copy-of select="&catts;"/>
        <strong><xsl:value-of select="@title"/>:</strong><br />
        <xsl:apply-templates/>
      </p>
    </xsl:template>

    <!-- list of alternate terms -->
    <xsl:template match="dtb:list">
    </xsl:template>
    
    <xsl:template match="dtb:source">
      <p class="source">
      	<strong>Source: </strong>
	<xsl:apply-templates/>
      </p>
    </xsl:template>


</xsl:stylesheet>
