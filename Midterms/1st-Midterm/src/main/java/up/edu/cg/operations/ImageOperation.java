package up.edu.cg.operations;

import up.edu.cg.model.ImageModel;

//interface to manage operations, this will allow to implement more opeartions in the future
public interface ImageOperation {
    ImageModel apply(ImageModel image);
}
