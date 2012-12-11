<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- parameter specifying class to strip -->
  <xsl:param name="strip-class"/>

  <xsl:template match="@*|*|processing-instruction()|comment()">
    <xsl:if test="string-length($strip-class) = 0 or (not(contains(@class, $strip-class)))"> 
      <xsl:copy>
	<xsl:apply-templates select="*|@*|text()|processing-instruction()|comment()"/>
      </xsl:copy>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
