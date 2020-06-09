#!/usr/bin/env python3
####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#
from	datetime import datetime, timedelta
from	typing import Dict
from	agn3.db import DB
from	agn3.runtime import CLI
#
class InProgress (CLI):
	def executor (self) -> bool:
		active = 0
		with DB () as db:
			companies: Dict[int, str] = {}
			now = datetime.now ()
			for row in db.queryc (
				'SELECT mdrop.status_id, mdrop.mailing_id, mdrop.genstatus, mdrop.genchange, mdrop.status_field, mt.shortname '
				'FROM maildrop_status_tbl mdrop INNER JOIN mailing_tbl mt ON mt.mailing_id = mdrop.mailing_id '
				'WHERE genchange > :limit AND (genstatus = 2 OR (genstatus = 1 AND status_field IN (\'A\', \'T\', \'W\')))',
				{
					'limit': datetime.fromordinal (now.toordinal () - 1)
				}
			):
				if self.is_active (now, row.genstatus, row.genchange, row.status_field):
					active += 1
					rqm = db.querys (
						'SELECT company_id, shortname '
						'FROM mailing_tbl '
						'WHERE mailing_id = :mailing_id',
						{'mailing_id': row.mailing_id}
					)
					if rqm is not None:
						try:
							company = companies[rqm.company_id]
						except KeyError:
							rqc = db.querys (
								'SELECT shortname '
								'FROM company_tbl '
								'WHERE company_id = :company_id',
								{'company_id': rqm.company_id}
							)
							company = companies[rqm.company_id] = rqc.shortname if rqc is not None else f'#{rqm.company_id}'
						status = 'is starting up to generate' if row.genstatus == 1 else 'is in generation'
						rqt = db.querys (
							'SELECT current_mails, total_mails '
							'FROM mailing_backend_log_tbl '
							'WHERE status_id = :status_id',
							{'status_id': row.status_id}
						)
						if rqt is None and row.status_field == 'W':
							rqt = db.querys (
								'SELECT current_mails, total_mails '
								'FROM world_mailing_backend_log_tbl '
								'WHERE mailing_id = :mailing_id',
								{'mailing_id': row.mailing_id}
							)
						if rqt is not None:
							status += ' (%d of %d are created)' % (rqt.current_mails, rqt.total_mails)
						else:
							status += ' (nothing created until now)'
						print ('Mailing %s (%d) for Company %s (%d) %s' % (row.shortname, row.mailing_id, company, rqm.company_id, status))
		if active > 0:
			print ('%d jobs still active' % active)
			return False
		return True

	def is_active (self, now: datetime, genstatus: int, genchange: datetime, status_field: str) -> bool:
		if genstatus == 2:
			if status_field in ('W', 'R', 'D'):
				offset = timedelta (hours = 2)
			else:
				offset = timedelta (minutes = 15)
		else:
			offset = timedelta (minutes = 10)
		return genchange + offset > datetime.now ()

if __name__ == '__main__':
	InProgress.main ()
