/* ========================================================================
This is the model on which the paper "Compositional Binding in Network
Domains" is based.
======================================================================== */

module alloy/fm06

/* ========================================================================
DOMAINS
======================================================================== */

sig Agent { }

sig Identifier { }

sig Name, Address extends Identifier { }
sig AddressPair extends Identifier { addr: Address, name: Name }



-- The meaning of an address pair lies in its content, so there are no
-- distinct address pairs with the same fields.
fact { all disj p1, p2: AddressPair |
   p1.addr != p2.addr || p1.name != p2.name }

sig Domain { 
   endpoints: set Agent,
   space: set Address,
   routing: space -> endpoints
}

sig Path { 
   source: Address,
   dest: Address,
   generator: Agent,
   absorber: Agent 
}

pred DomainSupportsPath [d: Domain, p: Path] { {
-- The source address routes to the generator.
   p.source in (d.routing).(p.generator)

-- The destination address routes to the absorber.
   p.absorber in (p.dest).(d.routing)
} }

/* ========================================================================
BINDINGS
======================================================================== */

sig Domain2 extends Domain {
   dstBinding: Identifier -> Identifier } {
   all i: Identifier | i in dstBinding.Identifier => 
   (  (i in Address => i in space) &&
      (i in AddressPair => i.addr in space)
   )
}

sig Path2 extends Path { 
   origDst: Identifier }

pred Domain2SupportsPath [d: Domain2, p: Path2] {
   DomainSupportsPath[d,p] &&
   p.dest in (p.origDst).(*(d.dstBinding)) &&
   p.dest !in (d.dstBinding).Identifier
} 

pred ReachableInDomain [d: Domain2, i: Identifier, g: Agent] {
   some a: Address |
      a in i.(*(d.dstBinding)) &&
      a !in (d.dstBinding).Identifier &&
      g in a.(d.routing)
}

pred DeterministicDomain [d: Domain2] {
   all i: Identifier | lone g: Agent | ReachableInDomain[d,i,g] }

pred NonloopingDomain [d: Domain2] { no ( ^(d.dstBinding) & iden ) }

pred AddBinding [d, d': Domain2, newBinding: Identifier -> Identifier] { {

-- Precondition: the new bindings can be applied in the domain.
   all i: Identifier | i in newBinding.Identifier => 
   (  (i in Address => i in d.space) &&
      (i in AddressPair => i.addr in d.space)
   )

-- Postconditions:
   d'.endpoints = d.endpoints
   d'.space = d.space
   d'.routing = d.routing
   d'.dstBinding = d.dstBinding + newBinding
} }

pred IdentifiersUnused [d: Domain2, new: Identifier ] { {
   no ((d.routing).Agent & new)
   no ((d.dstBinding).Identifier & new)
   no (Identifier.(d.dstBinding) & new)
} }




/* ========================================================================
DISTINCTIVE BINDINGS
======================================================================== */

sig Domain3 extends Domain2 {
   srcBinding: Identifier -> Identifier,
   AdstBinding: Identifier -> Identifier,
   BdstBinding: Identifier -> Identifier
} {
-- There are two kinds of destination binding.
   dstBinding = AdstBinding + BdstBinding
}

sig Path3 extends Path2 { 
   finSrc: Identifier }

pred Domain3SupportsPath [d: Domain3, p: Path3] {
   Domain2SupportsPath[d,p] &&
   p.finSrc in (p.source).(*(d.srcBinding)) &&
   p.finSrc !in (d.srcBinding).Identifier
}

pred ReturnPath [p1, p2: Path3] {
   p1.absorber = p2.generator &&
   p2.source = p1.dest &&
   p2.origDst = p1.finSrc
}

pred AddABinding [d1, d2: Domain3, newBinding: Identifier -> Identifier]{ {
  
-- Preconditions:
   IdentifiersUnused[d1,newBinding.Identifier]
   no ( Identifier.(d1.BdstBinding) & newBinding.Identifier ) 
   no (Identifier.newBinding & newBinding.Identifier)
                                                               
-- Postconditions:
   AddBinding[d1,d2,newBinding]
   d2.AdstBinding = d1.AdstBinding + newBinding
   d2.BdstBinding = d1.BdstBinding
   d2.srcBinding = d1.srcBinding
} }

pred AddBBinding [d1, d2: Domain3, newBinding: Identifier -> Identifier]{ {
  
-- Preconditions:
   IdentifiersUnused[d1,newBinding.Identifier]
   (all i: Identifier | lone i.newBinding )
   (all i: Identifier | lone (newBinding + d1.BdstBinding).i )
   no ( Identifier.newBinding & (d1.AdstBinding).Identifier ) 
   no (Identifier.newBinding & newBinding.Identifier)
                                                               
-- Postconditions:
   AddBinding[d1,d2,newBinding]
   d2.AdstBinding = d1.AdstBinding
   d2.BdstBinding = d1.BdstBinding + newBinding
   d2.srcBinding = d1.srcBinding + ~newBinding
} }

/* ========================================================================
STRUCTURE FOR RETURNABILITY AND STRUCTURAL VERIFICATION
======================================================================== */

pred StructuredDomain [d: Domain3] { 
   let ADom = (d.AdstBinding).Identifier,
       BDom = (d.BdstBinding).Identifier,
       RDom = (d.routing).Agent,
       BRan = Identifier.(d.BdstBinding)  | {

   NonloopingDomain[d]

-- The two bindings and routing operate on different identifiers.
   no (ADom & BDom)
   no (ADom & RDom)
   no (BDom & RDom)

-- Except for AdstBinding, delivering a message is deterministic.
   (all i: Identifier | lone i.(d.BdstBinding) )
   (all i: Identifier | lone i.(d.routing) )

-- B bindings are invertible.  Note that routing does not need to be 
-- invertible because dest retains the relevant history at the absorber.
   all i: Identifier | lone (d.BdstBinding).i

-- The source binding inverts (transposes) the BdstBinding.
   d.srcBinding = ~(d.BdstBinding)

-- Pattern A bindings precede Pattern B bindings.
   no ( BRan & ADom ) 
} }

pred RealStructure [d: Domain3, p: Path3] {
   StructuredDomain[d] && 
   Domain3SupportsPath[d,p] &&
   p.generator != p.absorber &&
   (some i1, i2: Identifier |
      i1 = (p.source).(d.srcBinding) && p.finSrc = i1.(d.srcBinding) &&
      i2 = (p.origDst).(d.AdstBinding) && p.dest = i2.(d.BdstBinding)
   )
}




/* ========================================================================
SINGLE-DOMAIN RETURNABILITY VERIFICATION
======================================================================== */

pred ReturnableDomain [d: Domain3] {
-- If there is a terminating attempt to return a path, it must go to
-- the generator of the message being returned.
(   all p1, p2: Path3 |
      Domain3SupportsPath[d,p1] && Domain3SupportsPath[d,p2] && 
      ReturnPath[p1,p2] 
   => p2.absorber = p1.generator
)  &&
-- If there is an attempt to return a path, it must terminate.
   NonloopingDomain[d] &&
(  all p1: Path3 | Domain3SupportsPath[d,p1] =>
      (all a: Address |
         a in (p1.finSrc).(*(d.dstBinding)) &&
         a !in (d.dstBinding).Identifier
      => a in (d.routing).Agent )
)
}


/* ========================================================================
DOUBLE-DOMAIN RETURNABILITY VERIFICATION
======================================================================== */

pred MobileAgentMove [g: Agent, a1, a2: Address, d1, d2: Domain3] { {
-- Preconditions:
-- a1 is the result of a B binding.
   a1 in Identifier.(d1.BdstBinding)
-- a1 is not in the domain of a B binding.
   a1 !in (d1.BdstBinding).Identifier
-- a1 routes to g.
   a1.(d1.routing) = g
-- a2 is unused.
   IdentifiersUnused[d1,a2]

-- Postconditions:
-- Update the domain, assuming structure.
   (let a3 = (d1.BdstBinding).a1 |
        d2.routing = d1.routing + (a2->g) - (a1->g) &&
        d2.BdstBinding = d1.BdstBinding + (a3->a2) - (a3->a1) &&
        d2.srcBinding = d1.srcBinding + (a2->a3) - (a1->a3)
      )
-- Frame conditions on domain parts that don't change:
   d2.endpoints = d1.endpoints
   d2.space = d1.space
   d2.AdstBinding = d1.AdstBinding
} }


pred RealMove [g: Agent, a1, a2: Address, d1, d2: Domain3, p1: Path3] { {
   StructuredDomain[d1] 
   MobileAgentMove[g,a1,a2,d1,d2]
   Domain3SupportsPath[d1,p1]
   p1.generator = g
   p1.generator != p1.absorber
   p1.source = a1
   (some i1, i2: Identifier |
      i1 = (p1.source).(d1.srcBinding) && 
      p1.finSrc = i1.(d1.srcBinding) &&
      i2 = (p1.origDst).(d1.AdstBinding) && 
      p1.dest = i2.(d1.BdstBinding)
   )
} }


pred MoveThenReturn [g: Agent, disj a1, a2: Address, d1, d2: Domain3, p1, p2: Path] { {
   StructuredDomain[d1]
   MobileAgentMove[g,a1,a2,d1,d2]
   Domain3SupportsPath[d1,p1]
   Domain3SupportsPath[d2,p2]
   ReturnPath[p1,p2]
   p1.generator = g
   p1.generator != p1.absorber
   p1.source = a1
   (some i1, i2: Identifier |
      i1 = (p1.source).(d1.srcBinding) && 
      p1.finSrc = i1.(d1.srcBinding) &&
      i2 = (p1.origDst).(d1.AdstBinding) && 
      p1.dest = i2.(d1.BdstBinding)
   )
} }

-- checked, this is the smallest possible scope



pred ReturnableDomainPair [d1, d2: Domain3] {
-- If there is a "return path", it must truly return.
(   all p1, p2: Path3 |
      Domain3SupportsPath[d1,p1] && Domain3SupportsPath[d2,p2] && 
      ReturnPath[p1,p2] 
   => p2.absorber = p1.generator
) &&
-- If there is an attempt to return a path, it must terminate.
   NonloopingDomain[d2] &&
(  all p1: Path3 | Domain3SupportsPath[d1,p1] =>
      (all a: Address |
         a in (p1.finSrc).(*(d2.dstBinding)) &&
         a !in (d2.dstBinding).Identifier
      => a in (d2.routing).Agent )
)
}

assert StructureSufficientForPairReturnability {
   all g: Agent, a1, a2: Address, d1, d2: Domain3 | 
      StructuredDomain[d1] &&
      MobileAgentMove[g,a1,a2,d1,d2]
      => ReturnableDomainPair[d1,d2]
}

check StructureSufficientForPairReturnability for 2 but
   2 Domain, 2 Path, 3 Agent, 9 Identifier

