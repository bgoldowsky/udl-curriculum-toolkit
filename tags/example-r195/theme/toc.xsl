<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE xsl:stylesheet[
 ]>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:wicket="http://wicket.apache.org"
    exclude-result-prefixes="dtb">
    
    <xsl:import href="dtbook2xhtml.xsl"/>
    <xsl:import href="common.xsl"/>

<!-- parameter "current" should be the ID of the section to be highlighted -->
<xsl:param name="current"/>
<!-- parameter "sectionLevel" should name the element type of sections (level2 or level3) -->
<xsl:param name="sectionLevel"/>

<xsl:template match="dtb:level1">
    <div class="collapseBody">
      <xsl:if test="dtb:*[local-name()!='level2' and local-name()!='h1']">
	<div class="tocImage">
	  <xsl:apply-templates select="dtb:*[local-name()!='level2' and local-name()!='h1']"/>
	</div>
      </xsl:if>
      <div>
	<xsl:if test="dtb:*[local-name()!='level2' and local-name()!='h1']">
	  <xsl:attribute name="class">tocImageOffset</xsl:attribute>
	</xsl:if>
	<xsl:apply-templates select="dtb:level2"/>
      </div>
    </div>
</xsl:template>

<xsl:template match="dtb:h1">
  <h2><xsl:apply-templates/></h2>
</xsl:template>

<xsl:template match="dtb:level2">
  <xsl:choose>
    <xsl:when test="$sectionLevel='level3' and ./dtb:level3">
      <!-- this node has children that are sections, so display header and dig deeper -->
      <xsl:apply-templates select="dtb:h2"/>
      <ul>
        <xsl:apply-templates select="dtb:level3"/>
      </ul>
    </xsl:when>
    <xsl:otherwise>
      <!-- this node is a section, link to content (needs ul/li for styling) -->
      <ul>
        <li>
          <xsl:call-template name="makelink"/>
        </li>
      </ul>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="dtb:h2">
  <h3><xsl:apply-templates/></h3>
</xsl:template>

<xsl:template match="dtb:level3">
  <li>
    <xsl:call-template name="makelink"/>
  </li>
</xsl:template>

<xsl:template name="makelink">
    <xsl:if test="@id = $current">
      <xsl:attribute name="class">current</xsl:attribute>
    </xsl:if>
    <div class="tocIcons">
      <a wicket:id="sectionStatusIcon_" href="#" id="{@id}" class="button icon" >
        <img wicket:id="doneImg" class="status" />
      </a>
      <xsl:text>&#xA0;</xsl:text>
      <img wicket:id="sectionIcon_" class="{@class}" alt="{@class}"/>
    </div>
    <div class="tocPageLinks">
      <a wicket:id="link_" href="{@id}">
        <xsl:apply-templates select="dtb:h2/node()|dtb:h3/node()"/>   
      </a>
      <span wicket:id="pageLinkPanel_" id="{@id}"></span>
    </div>
</xsl:template>

</xsl:stylesheet>
