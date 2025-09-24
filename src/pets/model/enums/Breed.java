package pets.model.enums;

public enum Breed {
	
    // DOG
    LABRADOR(PetType.DOG), PUG(PetType.DOG), GOLDEN_RETRIEVER(PetType.DOG), BULLDOG(PetType.DOG),
    GERMAN_SHEPHERD(PetType.DOG), BEAGLE(PetType.DOG), POODLE(PetType.DOG), BOXER(PetType.DOG),
    ROTTWEILER(PetType.DOG), DACHSHUND(PetType.DOG),

    // CAT
    SIAMESE(PetType.CAT), PERSIAN(PetType.CAT), MAINE_COON(PetType.CAT), BENGAL(PetType.CAT),
    SPHYNX(PetType.CAT), RAGDOLL(PetType.CAT), BRITISH_SHORTHAIR(PetType.CAT), SCOTTISH_FOLD(PetType.CAT),
    SAVANNAH(PetType.CAT), BURMESE(PetType.CAT),

    // BIRD
    PARAKEET(PetType.BIRD), COCKATIEL(PetType.BIRD), LOVE_BIRD(PetType.BIRD), AFRICAN_GREY(PetType.BIRD),
    MACAW(PetType.BIRD), CANARY(PetType.BIRD), FINCH(PetType.BIRD), DOVE(PetType.BIRD),
    COCKATOO(PetType.BIRD), AMAZON_PARROT(PetType.BIRD),

    // RABBIT
    HOLLAND_LOP(PetType.RABBIT), NETHERLAND_DWARF(PetType.RABBIT), MINI_REX(PetType.RABBIT), LIONHEAD(PetType.RABBIT),
    FLEMISH_GIANT(PetType.RABBIT), ENGLISH_LOP(PetType.RABBIT), HARLEQUIN(PetType.RABBIT), JERSEY_WOOLY(PetType.RABBIT),
    CHECKERED_GIANT(PetType.RABBIT), ANGORA(PetType.RABBIT),

    // HAMSTER
    SYRIAN(PetType.HAMSTER), ROBO(PetType.HAMSTER), CAMPBELL(PetType.HAMSTER), WINTER_WHITE(PetType.HAMSTER),
    CHINESE(PetType.HAMSTER), RUSSIAN_DWARF(PetType.HAMSTER), TURKISH_HAMSTER(PetType.HAMSTER), EUROPEAN_HAMSTER(PetType.HAMSTER),
    GREY_DWARF(PetType.HAMSTER), LONG_HAIRED(PetType.HAMSTER),

    // FISH
    GOLDFISH(PetType.FISH), BETTA(PetType.FISH), GUPPY(PetType.FISH), TETRA(PetType.FISH),
    ANGELFISH(PetType.FISH), MOLLY(PetType.FISH), PLATY(PetType.FISH), ZEBRAFISH(PetType.FISH),
    BARB(PetType.FISH), CATFISH(PetType.FISH);

    private final PetType petType;

    Breed(PetType petType) {
        this.petType = petType;
    }

    public PetType getPetType() {
        return petType;
    }
    
}