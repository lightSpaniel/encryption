import play.api.Configuration
import play.api.Environment
import play.api.inject.Binding
import play.api.inject.Module
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import play.api.mvc.Controller
import services.{Encrypt, Encryption}

class MyModule extends Module {
  def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {

    Seq(bind[Encryption].to[Encrypt])

  }
}
