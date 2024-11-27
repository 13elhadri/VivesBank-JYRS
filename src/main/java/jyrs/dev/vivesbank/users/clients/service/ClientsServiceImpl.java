package jyrs.dev.vivesbank.users.clients.service;


import jyrs.dev.vivesbank.users.clients.dto.ClientRequestCreate;
import jyrs.dev.vivesbank.users.clients.dto.ClientRequestUpdate;
import jyrs.dev.vivesbank.users.clients.dto.ClientResponse;
import jyrs.dev.vivesbank.users.clients.exceptions.ClientNotFound;
import jyrs.dev.vivesbank.users.clients.exceptions.ClienteExists;
import jyrs.dev.vivesbank.users.clients.mappers.ClientMapper;
import jyrs.dev.vivesbank.users.clients.models.Client;
import jyrs.dev.vivesbank.users.clients.repository.ClientsRepository;
import jyrs.dev.vivesbank.users.clients.storage.service.StorageService;
import jyrs.dev.vivesbank.users.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ClientsServiceImpl implements ClientsService {

    private final ClientsRepository repository;
    private final StorageService storageService;
    private final ClientMapper mapper;
    @Autowired

    public ClientsServiceImpl(ClientsRepository repository, StorageService storageService, ClientMapper mapper) {
        this.repository = repository;
        this.storageService = storageService;
        this.mapper = mapper;
    }

    @Override
    public Page<ClientResponse> getAll(Optional<String> nombre, Optional<String> apellidos,
                                       Optional<String> ciudad, Optional<String> provincia, Pageable pageable) {
        Specification<Client> specNombre = (root, query, criteriaBuilder) ->
                nombre.map(n -> criteriaBuilder.like(criteriaBuilder.lower(root.get("nombre")), "%" + n.toLowerCase() + "%"))
                        .orElse(null);

        Specification<Client> specApellido = (root, query, criteriaBuilder) ->
                apellidos.map(a -> criteriaBuilder.like(criteriaBuilder.lower(root.get("apellidos")), "%" + a.toLowerCase() + "%"))
                        .orElse(null);

        Specification<Client> specCiudad = (root, query, criteriaBuilder) ->
                ciudad.map(c -> criteriaBuilder.like(criteriaBuilder.lower(root.get("direccion").get("ciudad")), "%" + c.toLowerCase() + "%"))
                        .orElse(null);

        Specification<Client> specProvincia = (root, query, criteriaBuilder) ->
                provincia.map(p -> criteriaBuilder.like(criteriaBuilder.lower(root.get("direccion").get("provincia")), "%" + p.toLowerCase() + "%"))
                        .orElse(null);

        Specification<Client> criterio = Specification.where(specNombre)
                .and(specApellido)
                .and(specCiudad)
                .and(specProvincia);

        var page = repository.findAll(criterio, pageable);
        return page.map(mapper::toResponse);
    }

/*
    @Override
    public List<ClientResponse> getAllIsDeleted(Boolean isDeleted) {
        var lista = repository.getAllByIsDeleted(isDeleted);

        return lista.stream().map(mapper::toResponse).toList();
    }

 */


    @Override
    public ClientResponse getById(Long id) {
        var cliente = repository.findById(id).orElseThrow(() -> new ClientNotFound(id.toString()));
        return mapper.toResponse(cliente);
    }
/*
    @Override
    public ClientResponse getByUsername(String username) {
        var cliente = repository.getByUsername(username).orElseThrow(()->new ClientNotFound(username));
        return mapper.toResponse(cliente);
    }

 */

    @Override
    public ClientResponse getByDni(String dni) {
        var cliente = repository.getByDni(dni).orElseThrow(() -> new ClientNotFound(dni));
        return mapper.toResponse(cliente);
    }

    @Override
    public ClientResponse create(ClientRequestCreate clienteRequest, MultipartFile image,User user) {

        var cliente = mapper.toClientCreate(clienteRequest);


        repository.getByDni(cliente.getDni()).ifPresent(existingClient -> {
            throw new ClienteExists(cliente.getDni());
        });


        var tipo = "DNI-" + cliente.getEmail();
        String imageStored = storageService.store(image, tipo);
        String imageUrl = imageStored;

        cliente.setFotoDni(imageUrl);
        cliente.setUser(user);
        var clienteGuardado = repository.save(cliente);

        return mapper.toResponse(clienteGuardado);
    }

    @Override
    public ClientResponse update(Long id, ClientRequestUpdate clienteRequest) {
        var cliente = mapper.toClientUpdate(clienteRequest);

        var res = repository.findById(id).orElseThrow(() -> new ClientNotFound(id.toString()));

        res.setNombre(cliente.getNombre() != null ? cliente.getNombre() : res.getNombre());
        res.setApellidos(cliente.getApellidos() != null ? cliente.getApellidos() : res.getApellidos());
        res.setEmail(cliente.getEmail() != null ? cliente.getEmail() : res.getEmail());
        res.setDireccion(cliente.getDireccion() != null ? cliente.getDireccion() : res.getDireccion());
        res.setNumTelefono(cliente.getNumTelefono() != null ? cliente.getNombre() : res.getNombre());

        User user = res.getUser();
        user.setUsername(clienteRequest.getEmail() != null ? cliente.getEmail() : user.getUsername());
        user.setPassword(clienteRequest.getPassword() != null ? clienteRequest.getPassword() : user.getPassword());
        res.setUser(user);

        var clienteActualizado = repository.save(res);

        return mapper.toResponse(clienteActualizado);
    }

    @Override
    public ClientResponse updateDni(Long id, MultipartFile fotoDni) {
        var cliente = repository.findById(id).orElseThrow(() -> new ClientNotFound(id.toString()));

        var email = cliente.getEmail();
        var tipo = "DNI-" + email;
        String imageStored = storageService.store(fotoDni, tipo);
        storageService.delete(cliente.getFotoDni());

        cliente.setFotoDni(imageStored);

        var clienteActualizado = repository.save(cliente);

        return mapper.toResponse(clienteActualizado);
    }

    @Override
    public ClientResponse updatePerfil(Long id, MultipartFile fotoPerfil) {
        var cliente = repository.findById(id).orElseThrow(() -> new ClientNotFound(id.toString()));
        User user = cliente.getUser();
        var email = user.getUsername();
        var tipo = "PROFILE-" + email;
        String imageStored = storageService.store(fotoPerfil, tipo);
        storageService.delete(user.getUsername());

        user.setFotoPerfil(imageStored);

        cliente.setUser(user);

        var clienteActualizado = repository.save(cliente);

        return mapper.toResponse(clienteActualizado);
    }


    @Override
    public void delete(Long id) {
        var cliente = repository.findById(id).orElseThrow(() -> new ClientNotFound(id.toString()));

        repository.deleteById(id);
        storageService.delete(cliente.getFotoDni());

    }

    public void deleteLog(Long id) {
        var cliente = repository.findById(id).orElseThrow(() -> new ClientNotFound(id.toString()));

        User user = cliente.getUser();

        user.setIsDeleted(true);

        cliente.setUser(user);

        repository.save(cliente);
    }
}
