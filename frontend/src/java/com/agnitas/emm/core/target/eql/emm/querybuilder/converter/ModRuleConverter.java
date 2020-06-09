/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.querybuilder.converter;

import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderHelper;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderOperator;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderRuleNode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConversionException;
import org.apache.commons.lang3.ArrayUtils;

public class ModRuleConverter implements RuleConverter {
    @Override
    public String convert(QueryBuilderRuleNode ruleNode, int companyId) throws QueryBuilderToEqlConversionException {
        validate(ruleNode);
        Object[] values = QueryBuilderUtil.getRuleNodeValueAsArray(ruleNode);
        QueryBuilderOperator operator = QueryBuilderOperator.findByQueryBuilderName((String) values[1]);
        String strOperator = QueryBuilderHelper.relationalEqlOperator(operator);
        
        return String.format("`%s` %% %s %s %s", ruleNode.getId(), values[0], strOperator, values[2]);
    }

    private void validate(QueryBuilderRuleNode ruleNode) throws QueryBuilderToEqlConversionException {
        Object[] values = QueryBuilderUtil.getRuleNodeValueAsArray(ruleNode);
        if (ArrayUtils.getLength(values) != 3 || QueryBuilderUtil.containsAnyEmptyValue(values)) {
            throw new QueryBuilderToEqlConversionException();
        }
    }
}
