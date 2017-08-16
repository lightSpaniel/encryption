import javax.inject.Inject

import play.api.http.DefaultHttpFilters
import play.filters.headers.SecurityHeadersFilter
import play.filters.cors.CORSFilter
import play.filters.hosts.AllowedHostsFilter

class Filters @Inject() (securityHeadersFilter: SecurityHeadersFilter,
                         corsFilter: CORSFilter,
                         allowedHostsFilter: AllowedHostsFilter)
  extends DefaultHttpFilters(securityHeadersFilter, corsFilter, allowedHostsFilter)
