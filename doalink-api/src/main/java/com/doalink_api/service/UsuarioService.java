package com.doalink_api.service;

import com.doalink_api.dtos.request.ContatoRequest;
import com.doalink_api.dtos.request.UsuarioAtualizarRequest;
import com.doalink_api.dtos.response.ContatoResponse;
import com.doalink_api.dtos.response.UsuarioDetalheResponse;
import com.doalink_api.entities.Endereco;
import com.doalink_api.entities.Receptor;
import com.doalink_api.entities.Usuario;
import com.doalink_api.exception.ConflictException;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.mapper.EnderecoMapper;
import com.doalink_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public UsuarioDetalheResponse buscarPorId(Long id) {
        return UsuarioDetalheResponse.from(buscarEntidade(id));
    }

    @Transactional
    public UsuarioDetalheResponse atualizar(Long id, UsuarioAtualizarRequest req) {
        Usuario usuario = buscarEntidade(id);

        if (req.endereco() != null) {
            Endereco endereco = EnderecoMapper.toEndereco(req.endereco());
            if (usuario instanceof Receptor receptor) {
                receptor.setEnderecoOrganizacao(endereco);
            } else {
                usuario.setEndereco(endereco);
            }
        }

        return UsuarioDetalheResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public ContatoResponse obterContato(Long usuarioId) {
        return ContatoResponse.from(buscarEntidade(usuarioId));
    }

    @Transactional
    public ContatoResponse atualizarContato(Long usuarioId, ContatoRequest req) {
        Usuario usuario = buscarEntidade(usuarioId);

        if (req.email() != null && !req.email().isBlank()) {
            usuario.setEmailContato(req.email().trim().toLowerCase());
        }

        usuario.setTelefone(normalizarTelefone(req.telefone()));

        return ContatoResponse.from(usuarioRepository.save(usuario));
    }

    private Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado: " + id));
    }

    private String normalizarTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            return null;
        }
        String digits = telefone.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return null;
        }
        if (digits.length() < 10 || digits.length() > 11) {
            throw new ConflictException("Telefone inválido. Informe DDD e número com 10 ou 11 dígitos.");
        }
        return digits;
    }
}
