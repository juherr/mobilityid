## Mobility ID Utils

[![CI Scala](https://github.com/juherr/mobilityid/actions/workflows/ci-scala.yml/badge.svg)](https://github.com/juherr/mobilityid/actions/workflows/ci-scala.yml)

### About the library ###

Scala utils to parse, validate and convert electric mobility account
identifier strings according to the ISO 15118-1, DIN SPEC 91286 & EMI3 standards.

### Where to get it ###

The artifacts are published to Maven Central by the repository `Release` workflow, for Scala
2.13 and Scala 3 (built with the 3.9 LTS, so a Scala 3.9+ compiler is required):

``` scala
libraryDependencies += "dev.juherr.mobilityid" %% "mobilityid" % "X.Y.Z"
// compile-time checked interpolators (optional)
libraryDependencies += "dev.juherr.mobilityid" %% "mobilityid-interpolators" % "X.Y.Z"
```

The package stays `com.thenewmotion.mobilityid`: the code is the New Motion library, only the
Maven coordinates moved to the `dev.juherr.mobilityid` group shared with `mobilityid4j`.

### Development notes ###

This Scala codebase lives under `scala/` in the monorepo. Run every command from `scala/`.

- Build tool: sbt `2.0.9` (`project/build.properties`, `mise.toml`). sbt 2 runs a command
  sequence from one quoted argument (`sbt "a; b"`); the scripts use `sbt --server --batch` so
  that every invocation runs in a fresh JVM and sees the current environment.
- Scala versions: `2.13.18` and `3.9.0` (`build.sbt`); `sbt +test` runs both.
- Bytecode target: `-release 17` (Scala and javac).

```bash
sbt --server --batch "+test"
sbt --server --batch "core/testOnly com.thenewmotion.mobilityid.ContractIdSpec"
sbt --server --batch "+headerCheckAll; +scalafmtCheckAll; scalafmtSbtCheck; +scalafixAll --check; +test; +mimaReportBinaryIssues"
scripts/verify.sh   # the full gate + release guard wiring + consumer smoke, as the release preflight runs it
```

Format and fix in place with `sbt --server --batch "scalafmtSbt; +scalafmtAll; +scalafixAll"`.

If you use `mise` from the repository root, prefix the commands with `mise exec --`.

### Design and tooling decisions ###

- **Scala 2.13 + 3.9 LTS only.** Scala 3.9.0 (September 2026) is the LTS line library authors are
  expected to build on; it needs JDK 17+ to compile and run. Scala 2.12 and the previous 3.3 LTS
  were dropped before the first Maven Central release, so nothing published ever depended on
  them. `crossScalaVersions` carries exactly one Scala 3 version: two would publish the same
  `mobilityid_3` artifact.
- **JVM 17 baseline.** `-release 17` is the lowest target consistent with Scala 3.9; the
  Java port targets 21 but a Scala consumer on JDK 17 can still use this library.
- **specs2 4.x.** specs2 5 is Scala 3 only and the specs are shared between 2.13 and 3, so the
  build stays on the cross-published 4.x line (4.23.0).
- **Quality gates.** Scalafmt (`.scalafmt.conf`, no alignment, imports left to Scalafix),
  Scalafix (`.scalafix.conf`: `OrganizeImports` groups Scala/JDK, third-party, then project
  imports; `RemoveUnused` for imports, the only `-Wunused` category enabled), fatal warnings (`-Xfatal-warnings` / `-Werror`, `-Xlint` on 2.13,
  `-Wunused:imports` everywhere) and sbt-header on main and test sources.
- **Binary compatibility.** sbt-mima-plugin compares `core` and `interpolators` with the last
  release found on Maven Central (the repository `scripts/mima-baseline.sh` reads the `mobilityid_3`
  metadata, CI passes it as `-Dmobilityid.mimaBaseline=X.Y.Z`). Until the first release the
  check is skipped; `scripts/verify-release-wiring.sh` proves the wiring (resolution and
  analysis, not detection: the baseline is built from the same sources) against a locally
  published baseline.
- **Publishing.** sbt's built-in Central Portal support (`publishSigned` stages every Scala
  version in `target/sona-staging`, `sonaRelease` uploads the bundle) with sbt-pgp. The
  version is `RELEASE_VERSION` (set by the workflow), `0.1.0-SNAPSHOT` otherwise, and a
  `verifyRelease` guard refuses to sign a SNAPSHOT or to run without `SONATYPE_USERNAME`,
  `SONATYPE_PASSWORD`, `PGP_PASSPHRASE` and a GPG secret key; `scripts/verify-release-wiring.sh`
  also signs and stages a throw-away version with a temporary key to prove that path without
  uploading anything. sbt-ci-release was not used: it
  derives the version from an existing git tag, whereas the `Release` workflow tags after
  every registry has accepted the artifacts.
- **Consumer smoke.** `consumer-smoke/` is a separate sbt build that resolves both modules from
  an isolated Maven repository written by `scripts/verify-consumer.sh`, on both Scala versions.

### How to use ###

#### Contract Id ####

There are 3 types of Contract Id:
* DIN SPEC 91286, also known as EVCO-ID, e.g. NL-TNM-012204-5
* ISO 15118-1, also known as EMA-ID, e.g. NL-TNM-000122045-U
* EMI3, e.g. NL-TNM-C00122045-K

You can create an Contract Id object from a string in any of the 3 formats.  If you supply a check digit, it will be 
validated.  If it is not supplied, it will be calculated.

``` scala
root> project core
core> console
scala> import com.thenewmotion.mobilityid._
import ContractIdStandard._

scala> ContractId[DIN]("NL-TNM-012204-5")
res1: com.thenewmotion.mobilityid.ContractId[com.thenewmotion.mobilityid.ContractIdStandard.DIN] = NL-TNM-012204-5

scala> ContractId[ISO]("NL-TNM-000122045")
res2: com.thenewmotion.mobilityid.ContractId[com.thenewmotion.mobilityid.ContractIdStandard.ISO] = NL-TNM-000122045-U

scala> ContractId[EMI3]("NL-TNM-C00122045")
res3: com.thenewmotion.mobilityid.ContractId[com.thenewmotion.mobilityid.ContractIdStandard.EMI3] = NL-TNM-C00122045-K
```

This fails because of an illegal character:

``` scala
scala> ContractId[ISO]("NL-T|M-000122045")
java.lang.IllegalArgumentException: NL-T|M-000122045 is not a valid Contract Id for ISO 15118-1
```

If you have more detailed field information you can create a ContractId from the separate fields, choosing whether to
supply a check digit:

``` scala
scala> ContractId[ISO]("NL", "TNM", "000122045")
res4: com.thenewmotion.mobilityid.ContractId[com.thenewmotion.mobilityid.ContractIdStandard.ISO] = NL-TNM-000122045-U

scala> ContractId[ISO]("NL", "TNM", "000122045", 'U')
res5: com.thenewmotion.mobilityid.ContractId[com.thenewmotion.mobilityid.ContractIdStandard.ISO] = NL-TNM-000122045-U
```

This fails because of an invalid check digit:

``` scala
scala> ContractId[ISO]("NL", "TNM", "000122045", 'X')
java.lang.IllegalArgumentException: Given check digit 'X' is not equal to computed 'U'
```

This fails because of an illegal character:

```  scala
scala> ContractId[ISO]("NL", "T|M", "000122045")
java.lang.IllegalArgumentException: OperatorId must have a length of 3 and be ASCII letters or digits
```

Once you have an ContractId object you can convert it to another format:

``` scala
scala> ContractId[ISO]("NL-TNM-000122045").convertTo[DIN]
res1:  com.thenewmotion.mobilityid.ContractId[com.thenewmotion.mobilityid.ContractIdStandard.DIN] = NL-TNM-012204-5

scala> ContractId[DIN]("NL-TNM-012204-5").convertTo[EMI3]
res2: com.thenewmotion.mobilityid.ContractId[com.thenewmotion.mobilityid.ContractIdStandard.EMI3] = NL-TNM-C00122045-K
```

Also you can print them in various formats

``` scala
scala> val contractId = ContractId[ISO]("NL", "TNM", "012345678")
contractId: com.thenewmotion.mobilityid.ContractId[com.thenewmotion.mobilityid.ContractIdStandard.ISO] = NL-TNM-012345678-W

scala> contractId.toCompactString
res10: String = NLTNM012345678W

scala> contractId.toCompactStringWithoutCheckDigit
res11: String = NLTNM012345678

scala> contractId.toString
res12: String = NL-TNM-012345678-W
```

And you can get the party ID ("NL-TNM") which you can use to map a Contract Id to the provider that issued the token:

``` scala
scala> contractId.partyId
res13: com.thenewmotion.mobilityid.PartyId = NL-TNM
```

###### Notes

This library is using the algorithm of check digit calculation for ISO 15118-1 Contract IDs described here:
http://www.ochp.eu/id-validator/e-mobility-ids_evcoid_check-digit-calculation_explanation/

###### Changelog

`ContractId[ISO]` is roughly equivalent to the old `EmaId` class. 

`EmaId` could parse both DIN and ISO format, converting the former to the latter on the fly, e.g.

``` scala
scala> EmaId("NL-TNM-012204-5")
res0: com.thenewmotion.mobilityid.EmaId = NL-TNM-000122045-U
```

This will fail if replaced with `ContractId[ISO]` directly:

``` scala
scala> ContractId[ISO]("NL-TNM-012204-5")
java.lang.IllegalArgumentException: NL-TNM-012204-5 is not a valid Contract Id for ISO 15118-1
```
  
It is now necessary to parse the DIN and convert it to another format, e.g.

``` scala
scala> ContractId[DIN]("NL-TNM-012204-5").convertTo[ISO]
res0: com.thenewmotion.mobilityid.ContractId[com.thenewmotion.mobilityid.ContractIdStandard.ISO] = NL-TNM-000122045-U
```

Note that conversion from DIN to ISO format (as defined at https://github.com/e-clearing-net/OCHP/blob/master/OCHP.md#contractid-or-evco-id) 
is deprecated, and it is better to convert from DIN to EMI3 (as defined at http://emi3group.com/documents-links/)

``` scala
scala> ContractId[DIN]("NL-TNM-012204-5").convertTo[EMI3]
res1: com.thenewmotion.mobilityid.ContractId[com.thenewmotion.mobilityid.ContractIdStandard.EMI3] = NL-TNM-C00122045-K
```

#### EVSE-ID ####

You can create an EvseId object from a string in ISO 15118-1 or DIN SPEC 91286 format:

``` scala
scala> import com.thenewmotion.mobilityid._
import com.thenewmotion.mobilityid._

scala> EvseId("NL*TNM*E01225045")
res1: Option[com.thenewmotion.mobilityid.EvseId] = Some(NL*TNM*E01225045)

scala> EvseId("NLTNME01225045")
res2: Option[com.thenewmotion.mobilityid.EvseId] = Some(NL*TNM*E01225045)

scala> EvseId("+31*734*7734634")
res3: Option[com.thenewmotion.mobilityid.EvseId] = Some(31*734*7734634)
```

This fails because of an illegal character:

``` scala
scala> EvseId("NL*T|M*E01225045")
res4: Option[com.thenewmotion.mobilityid.EvseId] = None
```

If you have more detailed field information you can create an EvseId from the separate fields:

``` scala
scala> EvseId("NL", "TNM", "E000122045")
res5: com.thenewmotion.mobilityid.EvseId = NL*TNM*E000122045

scala> EvseId("+31", "734", "000122045")
res6: com.thenewmotion.mobilityid.EvseId = +31*734*000122045
```

This fails because of an illegal character in the operator id for DIN format:

```  scala
scala> EvseId("+31", "7A4", "000122045")
java.lang.IllegalArgumentException: Invalid operatorId for DIN format
```

This fails because powerOutletId must begin with 'E' when using ISO format

```  scala
scala> EvseId("NL", "TNM", "000122045")
java.lang.IllegalArgumentException: Invalid powerOutletId for ISO format
```

You can pattern match on whether the EVSEId is of ISO or DIN format:

```scala
scala> def getFormat(e: EvseId) = e match { case _: EvseIdIso => "IsIso" case _: EvseIdDin => "IsDin" }
getFormat: (e: com.thenewmotion.mobilityid.EvseId)String

scala> val evseIdIso = EvseId("NL", "TNM", "E000122045")
evseIdIso: com.thenewmotion.mobilityid.EvseId = NL*TNM*E000122045

scala> getFormat(evseIdIso)
res7: String = IsIso

scala> val evseIdDin = EvseId("+31", "734", "000122045")
evseIdDin: com.thenewmotion.mobilityid.EvseId = +31*734*000122045

scala> getFormat(evseIdDin)
res8: String = IsDin

```

Also you can print them in full format or compact format (only ISO standard):

``` scala
scala> val evseIdIso = EvseId("NL", "TNM", "E000122045")
evseIdIso: com.thenewmotion.mobilityid.EvseId = NL*TNM*E000122045

scala> evseIdIso.toString
res9: String = NL*TNM*E000122045

scala> evseIdIso match { case e: EvseIdIso => e.toCompactString }
res10: String = NLTNME000122045

scala> val evseIdDin = EvseId("+31", "734", "000122045")
evseIdDin: com.thenewmotion.mobilityid.EvseId = +31*734*000122045

scala> evseIdDin.toString
res11: String = +31*734*000122045

```

And you can get the party ID ("NL-TNM") which you can use to map an
EVSE-ID to the operator of the EVSE (only ISO standard):

``` scala
scala> evseIdIso.partyId
res12: com.thenewmotion.mobilityid.PartyId = NL-TNM
```

###### Notes

It is *NOT* possible to compare two EvseId objects where one is DIN format the other is ISO, as the 2 formats use different country identifiers and different operator codes


#### Party ID ####

As noted above, the library can give you `PartyId` instances representing the party that issued a token or operates an
EVSE. You can also creates instances of these from a String, so you can check if a certain token comes from a known
provider like this:

``` scala
scala> import com.thenewmotion.mobilityid.PartyId
import com.thenewmotion.mobilityid.PartyId

scala> val newMotionNetherlands = PartyId("NL*TNM").get
newMotionNetherlands: com.thenewmotion.mobilityid.PartyId = NL-TNM

scala> ContractId[ISO]("NL", "TNM", "000122045").partyId == newMotionNetherlands
res4: Boolean = true

scala> ContractId[ISO]("DE", "8LN", "000001292").partyId == newMotionNetherlands
res5: Boolean = false
```

#### Interpolators module ####

Can be imported with this dependency

``` scala
libraryDependencies += "dev.juherr.mobilityid" %% "mobilityid-interpolators" % "X.Y.Z"
```

then it can be used like this:

``` scala
scala> import com.thenewmotion.mobilityid.interpolators._
import com.thenewmotion.mobilityid.interpolators._

scala> evseId"ABC"
<console>:11: error: not a valid EvseId
              evseId"ABC"
                     ^

scala> evseId"NL*TNM*E840*6487"
res2: com.thenewmotion.mobilityid.EvseId = NL*TNM*E840*6487

scala> contractIdISO"ooopsie"
<console>:11: error: ooopsie is not a valid Contract Id for ISO 15118-1
                     contractIdISO"ooopsie"

scala> contractIdISO"NL-TNM-000722345-X"
res4: com.thenewmotion.mobilityid.ContractId[com.thenewmotion.mobilityid.ContractIdStandard.ISO] = NL-TNM-000722345-X
```
