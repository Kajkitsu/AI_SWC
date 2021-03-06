package pl.edu.wat.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.wat.demo.dtos.request.StartGainingCertificateRequest;
import pl.edu.wat.demo.dtos.response.GainedCertificateResponse;
import pl.edu.wat.demo.dtos.response.GainedStepResponse;
import pl.edu.wat.demo.entities.*;
import pl.edu.wat.demo.repositories.CertificateRepository;
import pl.edu.wat.demo.repositories.GainedCertificateRepository;
import pl.edu.wat.demo.repositories.GainedStepRepository;
import pl.edu.wat.demo.repositories.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class GainedCertificateServiceImpl implements GainedCertificateService {

    private final GainedCertificateRepository gainedCertificateRepository;
    private final UserRepository userRepository;
    private final CertificateRepository certificateRepository;
    private final GainedStepRepository gainedStepRepository;
    private final GainedStepService gainedStepService;
    private final FileService fileService;

    @Autowired
    public GainedCertificateServiceImpl(GainedCertificateRepository gainedCertificateRepository, UserRepository userRepository, CertificateRepository certificateRepository, GainedStepService gainedStepService, GainedStepRepository gainedStepRepository, FileService fileService) {
        this.gainedCertificateRepository = gainedCertificateRepository;
        this.userRepository = userRepository;
        this.certificateRepository = certificateRepository;
        this.gainedStepService = gainedStepService;
        this.gainedStepRepository = gainedStepRepository;
        this.fileService = fileService;
    }

    @Override
    public List<GainedCertificateResponse> getAll() {
        return StreamSupport.stream(gainedCertificateRepository.findAll().spliterator(), false)
                .map(gainedCertificateEntity -> new GainedCertificateResponse(
                        gainedCertificateEntity.getId(),
                        Optional.ofNullable(gainedCertificateEntity.getCollectDate()),
                        Optional.ofNullable(gainedCertificateEntity.getGainDate()),
                        java.util.Optional.ofNullable((gainedCertificateEntity.getFileEntity()!=null)?gainedCertificateEntity.getFileEntity().getId():null),
                        gainedCertificateEntity.isCollected(),
                        gainedCertificateEntity.isGained(),
                        gainedCertificateEntity.getUser().getId(),
                        gainedCertificateEntity.getCertificate().getId(),
                        gainedCertificateEntity.getCertificate().getName()
                )).collect(Collectors.toList());
    }

    public GainedCertificateResponse get(String id) {
        if(gainedCertificateRepository.findById(id).isPresent()){
            GainedCertificateEntity gainedCertificateEntity = gainedCertificateRepository.findById(id).get();
            return new GainedCertificateResponse(
                    gainedCertificateEntity.getId(),
                    Optional.ofNullable(gainedCertificateEntity.getCollectDate()),
                    Optional.ofNullable(gainedCertificateEntity.getGainDate()),
                    java.util.Optional.ofNullable((gainedCertificateEntity.getFileEntity()!=null)?gainedCertificateEntity.getFileEntity().getId():null),
                    gainedCertificateEntity.isCollected(),
                    gainedCertificateEntity.isGained(),
                    gainedCertificateEntity.getUser().getId(),
                    gainedCertificateEntity.getCertificate().getId(),
                    gainedCertificateEntity.getCertificate().getName()
            );
        }
        return null;
    }

    @Override
    public int startProcedure(StartGainingCertificateRequest certificateRequest) {
        if(certificateRepository.findById(certificateRequest.getCertificateID()).isPresent()){
            CertificateEntity certificateEntity = certificateRepository.findById(certificateRequest.getCertificateID()).get();
            if(userRepository.findById(certificateRequest.getUserId()).isPresent()){
                UserEntity userEntity = userRepository.findById(certificateRequest.getUserId()).get();
                int cost = certificateEntity.getCost();
                int userMoney = userEntity.getMoney();
                if((gainedCertificateRepository.findByCertificateAndUser(certificateEntity,userEntity).isEmpty()) && cost<=userMoney) {
                    userEntity.setMoney(userMoney-cost);
                    GainedCertificateEntity gainedCertificateEntity = new GainedCertificateEntity();
                    gainedCertificateEntity.setCertificate(certificateEntity);
                    gainedCertificateEntity.setUser(userEntity);
                    gainedCertificateEntity.setGained(false);
                    gainedCertificateEntity.setCollected(false);
                    StepEntity firstStep = certificateEntity.getFirstStep();
                    gainedCertificateRepository.save(gainedCertificateEntity);
                    userRepository.save(userEntity);
                    gainedStepService.startStep(firstStep.getId(),userEntity.getId(),gainedCertificateEntity);
                    return 1;
                }
                else {
                    return -2;
                }
            }

        }
        return -1;
    }

    @Override
    public List<GainedCertificateResponse> getUserCertificates(String user_id) {
        if(userRepository.findById(user_id).isPresent()){
            return gainedCertificateRepository.findByUser(userRepository.findById(user_id).get()).stream()
                    .map(gainedCertificateEntity -> new GainedCertificateResponse(
                            gainedCertificateEntity.getId(),
                            Optional.ofNullable(gainedCertificateEntity.getCollectDate()),
                            Optional.ofNullable(gainedCertificateEntity.getGainDate()),
                            java.util.Optional.ofNullable((gainedCertificateEntity.getFileEntity()!=null)?gainedCertificateEntity.getFileEntity().getId():null),
                            gainedCertificateEntity.isGained(),
                            gainedCertificateEntity.isCollected(),
                            gainedCertificateEntity.getUser().getId(),
                            gainedCertificateEntity.getCertificate().getId(),
                            gainedCertificateEntity.getCertificate().getName()
                    )).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public boolean removeById(String id) {
        if(gainedCertificateRepository.findById(id).isPresent()){
            gainedCertificateRepository.deleteById(id);
            return true;
        }
        else{
            return false;
        }

    }

    @Override
    public List<GainedCertificateResponse> getUserCertificatesFilterByConfirmed(String user_id, boolean confirmed) {
        if(userRepository.findById(user_id).isPresent()){
            return gainedCertificateRepository.findByUserAndGained(userRepository.findById(user_id).get(),confirmed).stream()
                    .map(gainedCertificateEntity -> new GainedCertificateResponse(
                            gainedCertificateEntity.getId(),
                            Optional.ofNullable(gainedCertificateEntity.getCollectDate()),
                            Optional.ofNullable(gainedCertificateEntity.getGainDate()),
                            java.util.Optional.ofNullable((gainedCertificateEntity.getFileEntity()!=null)?gainedCertificateEntity.getFileEntity().getId():null),
                            gainedCertificateEntity.isGained(),
                            gainedCertificateEntity.isCollected(),
                            gainedCertificateEntity.getUser().getId(),
                            gainedCertificateEntity.getCertificate().getId(),
                            gainedCertificateEntity.getCertificate().getName()
                    )).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public GainedCertificateResponse addFile(MultipartFile file, String id) {
        if(gainedCertificateRepository.findById(id).isPresent()){
            GainedCertificateEntity gainedCertificateEntity = gainedCertificateRepository.findById(id).get();
            gainedCertificateEntity.setFileEntity(fileService.storeFile(file));
            gainedCertificateRepository.save(gainedCertificateEntity);
            return new GainedCertificateResponse(
                    gainedCertificateEntity.getId(),
                    Optional.ofNullable(gainedCertificateEntity.getCollectDate()),
                    Optional.ofNullable(gainedCertificateEntity.getGainDate()),
                    java.util.Optional.ofNullable((gainedCertificateEntity.getFileEntity()!=null)?gainedCertificateEntity.getFileEntity().getId():null),
                    gainedCertificateEntity.isGained(),
                    gainedCertificateEntity.isCollected(),
                    gainedCertificateEntity.getUser().getId(),
                    gainedCertificateEntity.getCertificate().getId(),
                    gainedCertificateEntity.getCertificate().getName()
            );
        }
        return null;
    }

    @Override
    public GainedCertificateResponse confirmCollecting(String id) {
        if(gainedCertificateRepository.findById(id).isPresent() && !gainedCertificateRepository.findById(id).get().isCollected()){
            GainedCertificateEntity gainedCertificateEntity = gainedCertificateRepository.findById(id).get();
            gainedCertificateEntity.setCollected(true);
            gainedCertificateEntity.setCollectDate(new Date());
            gainedCertificateRepository.save(gainedCertificateEntity);
            return new GainedCertificateResponse(
                    gainedCertificateEntity.getId(),
                    Optional.ofNullable(gainedCertificateEntity.getCollectDate()),
                    Optional.ofNullable(gainedCertificateEntity.getGainDate()),
                    Optional.ofNullable((gainedCertificateEntity.getFileEntity()!=null)?gainedCertificateEntity.getFileEntity().getId():null),
                    gainedCertificateEntity.isCollected(),
                    gainedCertificateEntity.isGained(),
                    gainedCertificateEntity.getUser().getId(),
                    gainedCertificateEntity.getCertificate().getId(),
                    gainedCertificateEntity.getCertificate().getName()
            );
        }
        return null;
    }


}
