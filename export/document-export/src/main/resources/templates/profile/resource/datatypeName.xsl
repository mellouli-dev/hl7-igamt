<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template name="DatatypeName">
		<xsl:if test="normalize-space(@datatypeName)!=''">
<!-- 			<xsl:value-of disable-output-escaping="yes" select="concat('Usage Notes : ' ,@usageNotes)"></xsl:value-of>
 -->	
 			<b>Datatype Name : </b> <xsl:value-of disable-output-escaping="yes" select="@datatypeName"></xsl:value-of>
 		<xsl:element name="br" />
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>